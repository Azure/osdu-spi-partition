// Copyright 2022, Microsoft
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

package org.opengroup.osdu.partition.provider.azure.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.azure.data.tables.TableServiceClient;

@ExtendWith(MockitoExtension.class)
class TableStorageBootstrapConfigTest {

    @Mock
    private TableStorageBootstrapConfig tableStorageBootstrapConfig;

    @Mock
    private TableServiceClient tableServiceClient;

    @Test
    void testGetTableServiceClient() {
        // Mock the required dependencies
        String storageAccountEndpoint = "mock-storage-account-endpoint";
        when(tableStorageBootstrapConfig.getTableServiceClient(storageAccountEndpoint)).thenReturn(tableServiceClient);

        // Call the method under test
        TableServiceClient result = tableStorageBootstrapConfig.getTableServiceClient(storageAccountEndpoint);

        // Perform assertions
        assertNotNull(result);
        assertEquals(tableServiceClient, result);
    }
}
