/*
  Copyright 2002-2021 Google LLC
  Copyright 2002-2021 EPAM Systems, Inc

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

package org.opengroup.osdu.partition.coreplus.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.coreplus.model.PartitionPropertyEntity;
import org.opengroup.osdu.partition.coreplus.osm.repository.OsmPartitionPropertyRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PartitionServiceImplCacheTest {

    @Mock
    private ICache<String, PartitionInfo> partitionServiceCache;

    @Mock
    private ICache<String, List<String>> partitionListCache;

    @Mock
    private OsmPartitionPropertyRepository partitionPropertyEntityRepository;

    @Mock
    private AuditLogger auditLogger;

    private PartitionServiceImpl partitionServiceImpl;

    @BeforeEach
    public void setup() {
        partitionServiceImpl = new PartitionServiceImpl(
                partitionPropertyEntityRepository,
                auditLogger,
                partitionServiceCache,
                partitionListCache
        );
    }

    private List<PartitionPropertyEntity> partitionInfoToEntity(String partitionId, PartitionInfo partitionInfo) {
        return partitionInfo.getProperties().entrySet().stream()
                .map(entry -> new PartitionPropertyEntity(partitionId, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Test
    public void createPartitionSucceed() {
        String partId = "key";

        PartitionInfo newPi = PartitionInfo.builder().build();
        PartitionInfo retPi = PartitionInfo.builder().build();
        String propKey = "123987123498";
        retPi.getProperties().put(propKey, new Property());

        doReturn(Optional.empty(), Optional.of(partitionInfoToEntity(partId, retPi)))
                .when(partitionPropertyEntityRepository).findByPartitionId(partId);
        partitionServiceImpl.createPartition(partId, newPi);

        verify(partitionServiceCache, times(1))
                .put(any(), argThat(argument -> argument.getProperties().containsKey(propKey)));
        verify(partitionListCache).clearAll();
    }

        @Test
        public void createPartitionFailed() {
            String partId = "key";
            PartitionInfo newPi = PartitionInfo.builder().build();

            when(partitionServiceCache.get(partId)).thenReturn(null);
            doReturn(Optional.empty())
                    .when(partitionPropertyEntityRepository).findByPartitionId(partId);

            assertThrows(AppException.class, () -> partitionServiceImpl.createPartition(partId, newPi));

            verify(partitionServiceCache, times(0)).put(any(), any());
            verify(partitionListCache, times(0)).clearAll();
            verify(partitionServiceCache, times(2)).get(any());
        }

        @Test
        public void updatePartitionSucceed() {
            String partId = "key";

            PartitionInfo newPi = PartitionInfo.builder().build();
            PartitionInfo retPi = PartitionInfo.builder().build();
            String propKey = "123987123498";
            retPi.getProperties().put(propKey, new Property());

            doReturn(Optional.of(partitionInfoToEntity(partId, retPi)))
                    .when(partitionPropertyEntityRepository).findByPartitionId(partId);

            partitionServiceImpl.updatePartition(partId, newPi);

            verify(partitionServiceCache, times(1))
                    .put(any(), argThat(argument -> argument.getProperties().containsKey(propKey)));
        }

        @Test
        public void updatePartitionFailed() {
            String partId = "key";
            PartitionInfo newPi = PartitionInfo.builder().build();

            doReturn(Optional.empty())
                    .when(partitionPropertyEntityRepository).findByPartitionId(partId);

            assertThrows(AppException.class, () -> partitionServiceImpl.updatePartition(partId, newPi));

            verify(partitionServiceCache, times(0)).put(any(), any());
            verify(partitionServiceCache, times(0)).get(any());
        }


        @Test
        public void getPartition() {
            String partId = "key";

            PartitionInfo retPi = PartitionInfo.builder().build();
            String propKey = "123987123498";
            retPi.getProperties().put(propKey, new Property());

            doReturn(Optional.of(partitionInfoToEntity(partId, retPi)))
                    .when(partitionPropertyEntityRepository).findByPartitionId(partId);

            partitionServiceImpl.getPartition(partId);

            verify(partitionServiceCache, times(1)).get(partId);
            verify(partitionPropertyEntityRepository, times(1)).findByPartitionId(partId);
            verify(partitionServiceCache, times(1)).put(partId, retPi);
        }

        @Test
        public void deletePartition() {
            String partId = "key";
            PartitionInfo retPi = PartitionInfo.builder().build();

            doReturn(Optional.of(partitionInfoToEntity(partId, retPi)))
                    .when(partitionPropertyEntityRepository).findByPartitionId(partId);
            when(partitionServiceCache.get(partId)).thenReturn(retPi);

            partitionServiceImpl.deletePartition(partId);

            verify(partitionPropertyEntityRepository, times(1)).deleteByPartitionId(partId);
            verify(partitionServiceCache, times(1)).delete(partId);
            verify(partitionServiceCache, times(1)).get(partId);
            verify(partitionListCache, times(1)).clearAll();
        }

        @Test
        public void getAllPartitions() {
            List<String> partitions = new ArrayList<>();
            partitions.add("1");

            doReturn(partitions)
                    .when(partitionPropertyEntityRepository).getAllPartitions();

            partitionServiceImpl.getAllPartitions();

            verify(partitionListCache, times(1)).get(PartitionServiceImpl.PARTITION_LIST_KEY);
            verify(partitionPropertyEntityRepository, times(1)).getAllPartitions();
            verify(partitionListCache, times(1)).put(PartitionServiceImpl.PARTITION_LIST_KEY, partitions);
        }
}
