package org.opengroup.osdu.partition.api;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.CreatePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.DeletePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.UpdatePartitionDescriptor;
import org.opengroup.osdu.partition.util.BaseTestTemplate;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;

public abstract class UpdatePartitionTest extends BaseTestTemplate {

    protected String partitionId = getIntegrationTestPrefix() + System.currentTimeMillis();

    private String nonExistentPartitionId = "nonexistent-partition"+System.currentTimeMillis();

    public UpdatePartitionTest() {
        super(new UpdatePartitionDescriptor());
    }

    @Override
    protected String getId() {
        return partitionId;
    }

    @Override
    protected void deleteResource() throws Exception {
        DeletePartitionDescriptor deletePartitionDes = new DeletePartitionDescriptor();
        deletePartitionDes.setPartitionId(partitionId);
        CloseableHttpResponse response = deletePartitionDes.run(this.getId(), this.testUtils.getAccessToken());
        assertEquals(this.error(""), HttpStatus.NO_CONTENT.value(), response.getCode());
    }

    @Override
    protected void createResource() throws Exception {
        CreatePartitionDescriptor createPartitionDescriptor = new CreatePartitionDescriptor();
        createPartitionDescriptor.setPartitionId(partitionId);

        CloseableHttpResponse createResponse = createPartitionDescriptor.run(this.getId(), this.testUtils.getAccessToken());
        assertEquals(this.error(EntityUtils.toString(createResponse.getEntity())), HttpStatus.CREATED.value(),
                createResponse.getCode());
    }

    @Override
    protected int expectedOkResponseCode() {
        return HttpStatus.CREATED.value();
    }

    @Test
    public void should_return404_when_updatingNonExistentPartition() throws Exception {
        CloseableHttpResponse response = this.descriptor.run(nonExistentPartitionId, this.testUtils.getAccessToken());
        assertEquals(this.error(""), HttpStatus.NOT_FOUND.value(), response.getCode());
    }

    @Test
    public void should_return400_when_updatingPartitionWithIdField() throws Exception {
        createResource();
        CloseableHttpResponse response = this.descriptor.runWithCustomPayload(this.getId(), getInvalidBodyForUpdatePartition(), this.testUtils.getAccessToken());
        assertEquals(this.error(""), HttpStatus.BAD_REQUEST.value(), response.getCode());
        deleteResource();
    }

    @Test
    @Override
    public void should_return20XResponseCode_when_makingValidHttpsRequest() throws Exception {
        createResource();
        CloseableHttpResponse response = this.descriptor.runWithCustomPayload(this.getId(), getValidBodyForUpdatePartition(), this.testUtils.getAccessToken());
        deleteResource();
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getCode());
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH", response.getHeader("Access-Control-Allow-Methods").getValue());
        assertEquals("access-control-allow-origin, origin, content-type, accept, authorization, data-partition-id, correlation-id, appkey", response.getHeader("Access-Control-Allow-Headers").getValue());
        assertEquals("*", response.getHeader("Access-Control-Allow-Origin").getValue());
        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials").getValue());
        assertEquals("default-src 'self'", response.getHeader("Content-Security-Policy").getValue());
        assertEquals("max-age=31536000; includeSubDomains", response.getHeader("Strict-Transport-Security").getValue());
        assertEquals("0", response.getHeader("Expires").getValue());
        assertEquals("DENY", response.getHeader("X-Frame-Options").getValue());
        assertEquals("private, max-age=300", response.getHeader("Cache-Control").getValue());
        assertEquals("1; mode=block", response.getHeader("X-XSS-Protection").getValue());
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options").getValue());
    }

    private String getInvalidBodyForUpdatePartition() {
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        sb.append("  \"properties\": {")
                .append("\"elasticPassword\": {\"sensitive\":true,\"value\":\"test-password\"},")
                .append("\"serviceBusConnection\": {\"sensitive\":true,\"value\":\"test-service-bus-connection\"},")
                .append("\"complianceRuleSet\": {\"value\":\"shared\"},")
                .append("\"id\": {\"value\":\"test-id\"}")
                .append("}\n")
                .append("}");
        return sb.toString();
    }

    protected String getValidBodyForUpdatePartition() {
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        sb.append("  \"properties\": {")
                .append("\"updateElasticPassword\": {\"sensitive\":true,\"value\":\"test-password\"},")
                .append("\"serviceBusConnection\": {\"sensitive\":true,\"value\":\"test-service-bus-connection-update\"},")
                .append("\"complianceRuleSet\": {\"value\":\"shared\"}")
                .append("}\n")
                .append("}");
        return sb.toString();
    }
}
