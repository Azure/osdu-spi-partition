/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.provider.ibm.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.http.HttpClient;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.ibm.auth.ServiceCredentials;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.ibm.model.PartitionDoc;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.org.lightcouch.DocumentConflictException;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PartitionServiceImpl implements IPartitionService {

	private static final String PARTITION_DATABASE = "partition";
	private static final String PARTITION_LIST_KEY = "getAllPartitions";

	@Autowired
	private JaxRsDpsLog logger;

	@Autowired
	@Qualifier("partitionServiceCache")
	private ICache<String, PartitionInfo> partitionServiceCache;

	@Autowired
	@Qualifier("partitionListCache")
	private ICache<String, List<String>> partitionListCache;
	
	@Value("${pipline.trigger.url}")
	String pipelineURL;
	
	private final IHttpClient httpClient = new HttpClient();

	Database db;

	private IBMCloudantClientFactory cloudantFactory;

	@Value("${ibm.db.url}")
	private String dbUrl;
	@Value("${ibm.db.apikey:#{null}}")
	private String apiKey;
	@Value("${ibm.db.user:#{null}}")
	private String dbUser;
	@Value("${ibm.db.password:#{null}}")
	private String dbPassword;
	@Value("${ibm.env.prefix:local-dev}")
	private String dbNamePrefix;

	public PartitionServiceImpl() {

	}

	@PostConstruct
	public void init()  {
		cloudantFactory = new IBMCloudantClientFactory(new ServiceCredentials(dbUrl, dbUser, dbPassword));
		try {
			db = cloudantFactory.getDatabase(dbNamePrefix, PARTITION_DATABASE);
		} catch (MalformedURLException e) {
			log.error("malformed URL has occurred.", e);
			e.printStackTrace();
		}
	}

	@Override
	public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
		if (partitionServiceCache.get(partitionId) != null) {
			throw new AppException(HttpStatus.SC_CONFLICT, "partition exist", "Partition with same id exist");
		}

		PartitionInfo pi;
		PartitionDoc partitionDoc = new PartitionDoc(partitionId, partitionInfo);
		try {
			db.save(partitionDoc);
			pi = partitionInfo;
		} catch (DocumentConflictException e) {
			log.error("{} Partition already exists", partitionId);
			throw new AppException(e.getStatusCode(), "Conflict", "partition already exists");
		} catch (Exception e) {
			log.info("Partition creation failed ");
			e.printStackTrace();
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Partition creation failed", e);
		}

		if (pi != null) {
			partitionServiceCache.put(partitionId, pi);
			partitionListCache.clearAll();
		}
		
		if(partitionId.startsWith("integrationtest") || partitionId.startsWith("common")) {
			log.info("this partiton is created through integration test, skipping pipeline trigger");
			return pi;
		}
		HttpResponse response = runPipeline(partitionId);
		if(response.getResponseCode() != HttpStatus.SC_ACCEPTED || response.getResponseCode() != HttpStatus.SC_CREATED) {
			log.error("Response Recieved : "+response.getResponseCode()+"Error occured while triggering pipeline for record-changed-topic-listener [tenant-specific] deployment");
		}

		return pi;
	}

	private HttpResponse runPipeline(String partitionID) {
		JsonObject buildPostJsonData = buildPostJsonData(partitionID);
		Map<String, String> httpHeaders = new HashMap<>();
		httpHeaders.put("X-GitHub-Event", "push");
		httpHeaders.put("Content-Type", "application/json");
		//System.out.println("buildPostJsonData.getAsString() "+buildPostJsonData.getAsString());
		HttpResponse result = this.httpClient.send(
		            HttpRequest.post(buildPostJsonData).url(pipelineURL).headers(httpHeaders).build());
		    //this.getResult(result, Members.class);
		log.info("Pipeline trigger Response {}, Response Body : {}", result.getResponseCode(), result.getBody());
		return result;
	}
	
	private JsonObject buildPostJsonData(String partitionId) {
		JsonObject repositoryJson = new JsonObject();
		repositoryJson.addProperty("url", "https://unused-url-parameter-but-mandatory");
		repositoryJson.addProperty("name", partitionId);
		JsonObject postData = new JsonObject();
		postData.add("repository", repositoryJson);
		
		return postData;
	}

	@Override
	public PartitionInfo updatePartition(String partitionId, PartitionInfo partitionInfo) {
		PartitionInfo pi;
		if (partitionInfo.getProperties().containsKey("id")) {
			throw new AppException(HttpStatus.SC_BAD_REQUEST, "can not update id", "the field id can not be updated");
		}
		try {
			PartitionDoc partitionDoc = db.find(PartitionDoc.class, partitionId);
			partitionDoc.getPartitionInfo().getProperties().putAll(partitionInfo.getProperties());
			Response update = db.update(partitionDoc);
			pi = partitionDoc.getPartitionInfo();
		} catch (NoDocumentException e) {
			log.error(String.format("%s partition does not exists", partitionId));
			e.printStackTrace();
			throw new AppException(e.getStatusCode(), "Partition not found",
					String.format("%s Update failed. Create partition first. partition does not exists", partitionId),
					e);
		} catch (DocumentConflictException e) {
			log.error("Partition update failed. conflict is detected during the update");
			e.printStackTrace();
			throw new AppException(e.getStatusCode(), e.getReason(), e.getMessage(), e);
		} catch (Exception e) {
			log.error("Partition update failed");
			e.printStackTrace();
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Partition update failed", e.getMessage(), e);
		}

		if(pi != null) {
			partitionServiceCache.put(partitionId, pi);
		}

		return pi;
	}

	@Override
	public PartitionInfo getPartition(String partitionId) {
		PartitionInfo pi = partitionServiceCache.get(partitionId);

		if (pi == null) {
			PartitionDoc partitionDoc = null;
			try {
				partitionDoc = db.find(PartitionDoc.class, partitionId);
			} catch (NoDocumentException e) {
				log.error(String.format("%s partition does not exists", partitionId));
				e.printStackTrace();
				throw new AppException(e.getStatusCode(), e.getReason(), String.format("%s partition does not exists", partitionId), e);
			} catch (Exception e) {
				log.error("Partition could not found");
				e.printStackTrace();
				throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "unknown error", "Partition could not found", e );
			}
			pi = partitionDoc.getPartitionInfo();

			if (pi != null) {
				partitionServiceCache.put(partitionId, pi);
			}
		}

		return pi;
	}

	@Override
	public boolean deletePartition(String partitionId) {
		Response deleteStatus = null;
		try {
			PartitionDoc partitionDoc = db.find(PartitionDoc.class, partitionId);
			deleteStatus = db.remove(partitionDoc);
		} catch (NoDocumentException e) {
			log.error(String.format("Deletion failed. Could not find partition ", partitionId));
			e.printStackTrace();
			throw new AppException(e.getStatusCode(), e.getReason(), String.format("Deletion failed. Could not find partition %s", partitionId), e);
		} catch (Exception e) {
			log.error("Deletion Failed. Unexpected error");
			e.printStackTrace();
		}

		if(deleteStatus.getStatusCode() == 200) {
			if (partitionServiceCache.get(partitionId) != null) {
				partitionServiceCache.delete(partitionId);
			}
			partitionListCache.clearAll();
			return true;
		}

		return false;
	}

	@Override
	public List<String> getAllPartitions() {
		List<String> partitions = partitionListCache.get(PARTITION_LIST_KEY);

		if (partitions == null) {
			try {
				partitions = db.getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocIds();
			} catch (IOException e) {
				log.error("Partitions could not found. IOException occurred", e);
				e.printStackTrace();
			} catch (Exception e) {
				log.error("Partition could not found.", e);
				e.printStackTrace();
			}

			if (partitions != null) {
				partitionListCache.put(PARTITION_LIST_KEY, partitions);
			}
		}
		return partitions;
	}

}
