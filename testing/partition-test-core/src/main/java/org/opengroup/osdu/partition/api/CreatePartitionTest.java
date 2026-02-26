// Copyright 2017-2020, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.api;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.CreatePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.DeletePartitionDescriptor;
import org.opengroup.osdu.partition.util.BaseTestTemplate;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;

public abstract class CreatePartitionTest extends BaseTestTemplate {

    private String partitionId = getIntegrationTestPrefix() + System.currentTimeMillis();

    @Override
    protected String getId() {
        return partitionId;
    }

    @Override
    protected void deleteResource() throws Exception {
        DeletePartitionDescriptor deletePartitionDes = new DeletePartitionDescriptor();
        deletePartitionDes.setPartitionId(partitionId);
        CloseableHttpResponse response = deletePartitionDes.run(this.getId(), this.testUtils.getAccessToken());
    }

    @Override
    protected void createResource() throws Exception {
    }

    public CreatePartitionTest() {
        super(new CreatePartitionDescriptor());
    }

    @Override
    protected int expectedOkResponseCode() {
        return HttpStatus.CREATED.value();
    }

    @Test
    public void should_return409_when_creatingSamePartitionTwice() throws Exception {
        CloseableHttpResponse response = this.descriptor.run(this.getId(), testUtils.getAccessToken());
        assertEquals(this.error(""), this.expectedOkResponseCode(), response.getCode());

        CloseableHttpResponse response2 = this.descriptor.run(this.getId(), testUtils.getAccessToken());
        assertEquals(this.error(""), HttpStatus.CONFLICT.value(), response2.getCode());
        deleteResource();
    }

    @Test
    public void should_return40XResponseCode_when_makingRequest_withInvalidPayload() throws Exception {
        String invalidPayload = "";
        CloseableHttpResponse response = descriptor.runWithCustomPayload(getId(), invalidPayload, testUtils.getAccessToken());
        assertEquals(400, response.getCode());
    }
}
