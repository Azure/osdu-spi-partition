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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.GetPartitionDescriptor;
import org.opengroup.osdu.partition.util.BaseTestTemplate;
import org.opengroup.osdu.partition.util.Config;
import org.opengroup.osdu.partition.util.TestTokenUtils;
import org.opengroup.osdu.partition.util.TestUtils;
import org.springframework.http.HttpStatus;

public final class GetPartitionByIdApiTest extends BaseTestTemplate {

    private String partitionId = Config.Instance().osduTenant;

    @Override
    @Before
    public void setup() {
        this.testUtils = new TestTokenUtils();
    }

    @Override
    @After
    public void tearDown() {
        this.testUtils = null;
    }

    @Override
    protected String getId() {
        return partitionId;
    }

    public GetPartitionByIdApiTest() {
        super(new GetPartitionDescriptor());
    }

    @Override
    protected int expectedOkResponseCode() {
        return HttpStatus.OK.value();
    }

    @Test
    public void read_partition() throws Exception {
        CloseableHttpResponse response = this.descriptor.run(this.getId(), this.testUtils.getAccessToken());
        Object partitionProperties = TestUtils.parseResponse(response);
        
        assertNotNull(partitionProperties);
        assertEquals(HttpStatus.OK.value(), response.getCode());
    }

    @Test
    public void read_not_existing_partition() throws Exception {
        CloseableHttpResponse response = this.descriptor.run("not-existing-partition", this.testUtils.getAccessToken());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getCode());
    }
}
