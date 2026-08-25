/*
 Copyright 2002-2023 Google LLC
 Copyright 2002-2023 EPAM Systems, Inc

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

package org.opengroup.osdu.partition.api;

import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.partition.util.BasePartitionAcceptanceTests;

public final class HealthCheckApiTest extends BasePartitionAcceptanceTests {

    @Test
    public void should_returnOk() {
        HttpResponse<Void> response = partitionClient.livenessCheck();
        assertEquals(SC_OK, response.statusCode());
    }

    @Test
    public void should_returnOkWithTrailingSlash() {
        HttpResponse<Void> response = partitionClient.livenessCheckTrailingSlash();
        assertEquals(SC_OK, response.statusCode());
    }
}
