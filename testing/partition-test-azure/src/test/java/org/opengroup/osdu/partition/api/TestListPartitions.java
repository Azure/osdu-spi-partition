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
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.CreatePartitionDescriptor;
import org.opengroup.osdu.partition.util.AzureTestUtils;
import org.opengroup.osdu.partition.util.RestDescriptor;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertTrue;

public class TestListPartitions extends ListPartitionsApitTest {

    @Before
    @Override
    public void setup() {
        this.testUtils = new AzureTestUtils();
    }

    @After
    @Override
    public void tearDown() {
        this.testUtils = null;
    }

    @Test
    @Override
    public void should_return401_when_noAccessToken() throws Exception {
        // revisit this later -- Istio is changing the response code
    }

    @Test
    @Override
    public void should_return401_when_accessingWithCredentialsWithoutPermission() throws Exception {
        // revisit this later -- Istio is changing the response code
    }

    @Test
    @Override
    public void should_return401_when_makingHttpRequestWithoutToken() throws Exception {
        // revisit this later -- Istio is changing the response code
    }

    @Test
    public void should_notReturnSystemPartition_when_listPartitionIsCalled() throws Exception {
        //check by creation of system partition should give either 201, or 409
        CloseableHttpResponse createResponse = createPartition("system");

        assertTrue(createResponse.getCode()== HttpStatus.CREATED.value() || createResponse.getCode() == HttpStatus.CONFLICT.value());

        CloseableHttpResponse listPartitionsResponse = this.descriptor.run(this.getId(), this.testUtils.getAccessToken());

        String responseBody = EntityUtils.toString(listPartitionsResponse.getEntity());
        assertTrue(!responseBody.contains("system"));
    }

    private CloseableHttpResponse createPartition(String partitionId) throws Exception {
        CreatePartitionDescriptor createPartition = new CreatePartitionDescriptor();

        createPartition.setPartitionId(partitionId);

        RestDescriptor oldDescriptor = this.descriptor;

        this.descriptor = createPartition;

        CloseableHttpResponse createResponse = this.descriptor.run(partitionId, this.testUtils.getAccessToken());


        this.descriptor = oldDescriptor;
        return createResponse;
    }

}
