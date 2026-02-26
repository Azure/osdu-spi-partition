// Copyright © 2020 Amazon Web Services
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
import org.opengroup.osdu.partition.util.AwsTestUtils;

import static org.junit.Assert.assertEquals;

public class TestDeletePartition extends DeletePartitionTest {
    
    @Before
    @Override
    public void setup() {
        this.testUtils = new AwsTestUtils();
    }

    @After
    @Override
    public void tearDown() {

        try {
            this.deleteResource();
        }
        catch (Exception e) {
            
        }
        this.testUtils = null;
    }

    @Override
    @Test
    public void should_return20XResponseCode_when_makingValidHttpsRequest() throws Exception {
        createResource();
        CloseableHttpResponse response = descriptor.run(getId(), testUtils.getAccessToken());
        deleteResource();
        assertEquals(error(response.getCode() == 204 ? "" : EntityUtils.toString(response.getEntity())), expectedOkResponseCode(), response.getCode());
        assertEquals("default-src 'self'", response.getHeader("Content-Security-Policy").getValue());
        assertEquals("max-age=31536000; includeSubDomains", response.getHeader("Strict-Transport-Security").getValue());
        assertEquals("0", response.getHeader("Expires").getValue());
        assertEquals("DENY", response.getHeader("X-Frame-Options").getValue());
        assertEquals("private, max-age=300", response.getHeader("Cache-Control").getValue());
        assertEquals("1; mode=block", response.getHeader("X-XSS-Protection").getValue());
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options").getValue());
    }
}
