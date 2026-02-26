/*
 * Copyright © Amazon Web Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.provider.aws.service;

import java.util.List;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.aws.v2.dynamodb.DynamoDBConfig;
import org.opengroup.osdu.core.aws.v2.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.partition.provider.aws.model.PartitionDoc;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Service
public class PartitionServiceImpl implements IPartitionService {

    private static final String PARTITION_NOT_FOUND = "Partition not found";
    private static final String PARTITION_EXISTS = "Partition with same id exists";
    private static final String CANNOT_UPDATE_ID = "The field id cannot be updated";

    private final JaxRsDpsLog logger;
    private final DynamoDBQueryHelper<PartitionDoc> queryHelper;

    @Autowired
    public PartitionServiceImpl(JaxRsDpsLog logger, ProviderConfigurationBag config) {
        this.logger = logger;
        DynamoDBConfig dynamoConfig = DynamoDBConfig.builder()
                .region(config.amazonRegion)
                .build();
        DynamoDbEnhancedClient enhancedClient = dynamoConfig.dynamoDbEnhancedClient();
        DynamoDbTable<PartitionDoc> table = enhancedClient.table(
                config.dynamodbTableName,
                TableSchema.fromBean(PartitionDoc.class)
        );
        this.queryHelper = DynamoDBQueryHelper.<PartitionDoc>builder()
                .client(enhancedClient)
                .itemType(PartitionDoc.class)
                .table(table)
                .build();
    }

    @Override
    public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
        PartitionDoc partition = PartitionDoc.create(partitionId, partitionInfo);

        if (queryHelper.getItem(partitionId).isPresent()) {
            logger.error("Attempted to create duplicate partition: " + partitionId);
            throw new AppException(HttpStatus.SC_CONFLICT, "partition exist", PARTITION_EXISTS);
        }

        try {
            queryHelper.putItem(partition);
            logger.info("Created partition: " + partitionId);
            return partition.getPartitionInfo();
        } catch (Exception e) {
            logger.error("Failed to create partition: " + partitionId, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Failed to create partition", e.getMessage());
        }
    }

    @Override
    public PartitionInfo updatePartition(String partitionId, PartitionInfo partitionInfo) {
        validateInput(partitionId, partitionInfo);
        validateNoIdUpdate(partitionInfo);

        PartitionDoc partition = getPartitionOrThrow(partitionId);
        PartitionInfo existing = partition.getPartitionInfo();

        boolean hasChanges = false;
        for (var entry : partitionInfo.getProperties().entrySet()) {
            Property oldValue = existing.getProperties().putIfAbsent(entry.getKey(), entry.getValue());
            /*
             * If the oldValue is null, then the property did not already exist. See:
             * https://docs.oracle.com/javase/8/docs/api/java/util/Map.html#putIfAbsent-K-V-
             */
            hasChanges = (oldValue == null || hasChanges); 
        }

        if (hasChanges) {
            try {
                queryHelper.putItem(PartitionDoc.create(partitionId, existing));
                logger.info("Updated partition: " + partitionId);
            } catch (Exception e) {
                logger.error("Failed to update partition: " + partitionId, e);
                throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        "Failed to update partition", e.getMessage());
            }
        } else {
            logger.info("No changes detected for partition: " + partitionId);
        }

        return existing;
    }

    @Override
    public PartitionInfo getPartition(String partitionId) {
        validatePartitionId(partitionId);
        return getPartitionOrThrow(partitionId).getPartitionInfo();
    }

    @Override
    public boolean deletePartition(String partitionId) {
        validatePartitionId(partitionId);
        getPartitionOrThrow(partitionId);

        try {
            queryHelper.deleteItem(partitionId);
            logger.info("Deleted partition: " + partitionId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete partition: " + partitionId, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Failed to delete partition", e.getMessage());
        }
    }

    @Override
    public List<String> getAllPartitions() {
        try {
            return queryHelper.scanTable().stream()
                    .map(PartitionDoc::getId)
                    .toList();
        } catch (Exception e) {
            logger.error("Failed to retrieve all partitions", e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Failed to retrieve partitions", e.getMessage());
        }
    }

    private void validateInput(String partitionId, PartitionInfo partitionInfo) {
        validatePartitionId(partitionId);
        if (partitionInfo == null) {
            throw new IllegalArgumentException("PartitionInfo cannot be null");
        }
    }

    private void validatePartitionId(String partitionId) {
        if (partitionId == null || partitionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Partition ID cannot be null or empty");
        }
    }

    private void validateNoIdUpdate(PartitionInfo partitionInfo) {
        if (partitionInfo.getProperties().containsKey("id")) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST,
                    "Cannot update id", CANNOT_UPDATE_ID);
        }
    }

    private PartitionDoc getPartitionOrThrow(String partitionId) {
        PartitionDoc partition = queryHelper.getItem(partitionId).orElse(null);
        if (partition == null) {
            logger.error("Partition not found: " + partitionId);
            throw new AppException(HttpStatus.SC_NOT_FOUND, PARTITION_NOT_FOUND,
                    String.format("%s partition not found", partitionId));
        }
        return partition;
    }
}
