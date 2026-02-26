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
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.ListPartitionDescriptor;
import org.opengroup.osdu.partition.util.BaseTestTemplate;
import org.opengroup.osdu.partition.util.Config;
import org.opengroup.osdu.partition.util.TestTokenUtils;
import org.opengroup.osdu.partition.util.TestUtils;
import org.springframework.http.HttpStatus;

public final class ListPartitionsApiTest extends BaseTestTemplate {

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

    public ListPartitionsApiTest() {
        super(new ListPartitionDescriptor());
    }

    @Override
    protected int expectedOkResponseCode() {
        return HttpStatus.OK.value();
    }

    @Test
    public void retrieve_partition_list() throws Exception {
        CloseableHttpResponse response = this.descriptor.run(null, this.testUtils.getAccessToken());
        Object partitionIds = TestUtils.parseResponse(response);

        Assert.assertNotNull(partitionIds);
        // assertTrue(partitionIds.contains(partitionId));
        assertEquals(HttpStatus.OK.value(), response.getCode());
    }
}
