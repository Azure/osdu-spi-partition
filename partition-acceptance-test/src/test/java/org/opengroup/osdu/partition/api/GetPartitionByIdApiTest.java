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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Method;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.partition.PartitionInfo;
import org.opengroup.osdu.partition.util.BasePartitionAcceptanceTests;

public final class GetPartitionByIdApiTest extends BasePartitionAcceptanceTests {

    @Test
    public void read_partition() {
        HttpResponse<PartitionInfo> response = partitionClient.getPartition(servicesConfig.getDataPartitionId());

        assertNotNull(response.body());
        assertEquals(HttpStatus.SC_OK, response.statusCode());
    }

    @Test
    public void read_not_existing_partition() {
        ClientException ex = assertThrows(ClientException.class,
            () -> partitionClient.getPartition("not-existing-partition"));

        assertEquals(HttpStatus.SC_NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void should_return400_when_makingHttpRequestWithoutValidUrl() throws Exception {
        String invalidPath = "partitions/" + servicesConfig.getDataPartitionId() + "//";
        assertEquals(HttpStatus.SC_BAD_REQUEST, send(invalidPath, Method.GET).statusCode());
    }
}
