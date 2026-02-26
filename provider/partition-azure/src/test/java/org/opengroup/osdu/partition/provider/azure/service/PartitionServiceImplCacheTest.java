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

package org.opengroup.osdu.partition.provider.azure.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.azure.config.AzureConfig;
import org.opengroup.osdu.partition.provider.azure.persistence.PartitionTableStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PartitionServiceImplCacheTest {

    @Mock
    private PartitionTableStore tableStore;

    @Mock
    private ICache<String, PartitionInfo> partitionServiceCache;

    @Mock
    private ICache<String, List<String>> partitionListCache;

    @InjectMocks
    private PartitionServiceImpl partitionService;

    @Mock
    private AzureConfig azureConfig;


    @Test
    public void createPartitionSucceed() {
        String partId = "key";

        PartitionInfo newPi = PartitionInfo.builder().build();
        PartitionInfo retPi = PartitionInfo.builder().build();

        PartitionInfo partition = partitionService.createPartition(partId, newPi);

        assertEquals(newPi, partition);
        verify(tableStore, times(1)).addPartition(partId, newPi);
        verify(partitionServiceCache, times(1)).put(partId, retPi);
        verify(partitionListCache, times(1)).clearAll();
    }

    @Test
    public void updatePartitionSucceed() {
        String partId = "key";

        PartitionInfo newPi = PartitionInfo.builder().build();
        Map<String, Property> retPiProps = new HashMap<>();
        PartitionInfo retPi = PartitionInfo.builder().properties(retPiProps).build();

        when(tableStore.partitionExists(partId)).thenReturn(true);
        when(tableStore.getPartition(partId)).thenReturn(retPiProps);

        partitionService.updatePartition(partId, newPi);

        verify(tableStore, times(1)).addPartition(partId, newPi);
        verify(partitionServiceCache, times(1)).put(partId, retPi);
    }

    @Test
    public void updatePartitionFailed() {
        String partId = "key";
        PartitionInfo newPi = PartitionInfo.builder().build();

        when(tableStore.partitionExists(partId)).thenReturn(true);
        when(tableStore.getPartition(partId)).thenReturn(null);

        partitionService.updatePartition(partId, newPi);

        verify(tableStore, times(1)).addPartition(partId, newPi);
        verify(partitionServiceCache, times(0)).put(any(), any());
        verify(partitionServiceCache, times(0)).get(any());
    }

    @Test
    public void getPartition() {
        String partId = "key";

        Map<String, Property> retPiProps = new HashMap<String, Property>() {{
            put("1", mock(Property.class));
        }};
        PartitionInfo retPi = PartitionInfo.builder().properties(retPiProps).build();

        when(tableStore.getPartition(partId)).thenReturn(retPiProps);

        partitionService.getPartition(partId);

        verify(partitionServiceCache, times(1)).get(partId);
        verify(tableStore, times(1)).getPartition(partId);
        verify(partitionServiceCache, times(1)).put(partId, retPi);
    }

    @Test
    public void deletePartition() {
        String partId = "key";
        PartitionInfo retPi = PartitionInfo.builder().build();

        when(tableStore.partitionExists(partId)).thenReturn(true);
        when(partitionServiceCache.get(partId)).thenReturn(retPi);

        partitionService.deletePartition(partId);

        verify(tableStore, times(1)).deletePartition(partId);
        verify(partitionServiceCache, times(1)).delete(partId);
        verify(partitionServiceCache, times(1)).get(partId);
        verify(partitionListCache, times(1)).clearAll();
    }

    @Test
    public void getAllPartitions() {
        List<String> partitions = new ArrayList<>();
        when(tableStore.getAllPartitions()).thenReturn(partitions);

        partitionService.getAllPartitions();

        String partKey = "getAllPartitions";
        verify(partitionListCache, times(1)).get(partKey);
        verify(tableStore, times(1)).getAllPartitions();
        verify(partitionListCache, times(1)).put(partKey, partitions);
    }
}
