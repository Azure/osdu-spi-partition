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

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.azure.config.AzureConfig;
import org.opengroup.osdu.partition.provider.azure.persistence.PartitionTableStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PartitionServiceImplTest {

    @Mock
    private ICache<String, PartitionInfo> partitionServiceCache;

    @Mock
    private ICache<String, List<String>> partitionListCache;

    @Mock
    private PartitionTableStore tableStore;
    @InjectMocks
    private PartitionServiceImpl sut;
    @Mock
    private AzureConfig azureConfig;
    private final PartitionInfo partitionInfo = new PartitionInfo();

    private final static String PARTITION_ID = "my-tenant";
    private final Map<String, Property> properties = new HashMap<>();

    @BeforeEach
    public void setup() {
        properties.put("id", Property.builder().value(PARTITION_ID).build());
        properties.put("storageAccount", Property.builder().value("storage-account").sensitive(true).build());
        properties.put("complianceRuleSet", Property.builder().value("compliance-rule-set").build());
        partitionInfo.setProperties(properties);
    }

    @Test
    public void should_ThrowConflictError_when_createPartition_whenPartitionExistsInCache() {
        when(partitionServiceCache.get(PARTITION_ID)).thenReturn(this.partitionInfo);

        try {
            sut.createPartition(PARTITION_ID, this.partitionInfo);
        } catch (AppException e) {
            assertEquals(409, e.getError().getCode());
            assertTrue(e.getError().getReason().equalsIgnoreCase("partition exist"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("Partition with same id exist"));
        }
    }

    @Test
    public void should_ThrowConflictError_when_createPartition_whenPartitionExists() {
        when(this.tableStore.partitionExists(PARTITION_ID)).thenReturn(true);

        try {
            sut.createPartition(PARTITION_ID, this.partitionInfo);
        } catch (AppException e) {
            assertEquals(409, e.getError().getCode());
            assertTrue(e.getError().getReason().equalsIgnoreCase("partition exist"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("Partition with same id exist"));
        }
    }

    @Test
    public void should_returnPartitionInfo_when_createPartition_whenPartitionDoesNotExist() {
        when(this.tableStore.partitionExists(PARTITION_ID)).thenReturn(false);

        PartitionInfo partInfo = sut.createPartition(PARTITION_ID, this.partitionInfo);

        assertEquals(3, partInfo.getProperties().size());
        assertTrue(partInfo.getProperties().containsKey("id"));
        assertTrue(partInfo.getProperties().containsKey("complianceRuleSet"));
        assertTrue(partInfo.getProperties().containsKey("storageAccount"));
    }

    @Test
    public void should_ThrowNotFoundError_when_updatePartition_whenPartitionDoesNotExist() {
        when(this.tableStore.partitionExists(PARTITION_ID)).thenReturn(false);

        try {
            sut.updatePartition(PARTITION_ID, this.partitionInfo);
        } catch (AppException e) {
            assertEquals(404, e.getError().getCode());
            assertTrue(e.getError().getReason().equalsIgnoreCase("partition not found"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("my-tenant partition not found"));
        }
    }

    @Test
    public void should_ThrowBadRequestError_when_updatePartition_whenUpdatingPartitionId() {
        when(this.tableStore.partitionExists(PARTITION_ID)).thenReturn(true);

        try {
            sut.updatePartition(PARTITION_ID, this.partitionInfo);
        } catch (AppException e) {
            assertEquals(400, e.getError().getCode());
            assertTrue(e.getError().getReason().equalsIgnoreCase("can not update id"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("the field id can not be updated"));
        }
    }

    @Test
    public void should_returnPartition_when_partitionExistsInCache() {
        when(partitionServiceCache.get(PARTITION_ID)).thenReturn(this.partitionInfo);

        PartitionInfo partitionInfo = this.sut.getPartition(PARTITION_ID);

        assertTrue(partitionInfo.getProperties().containsKey("storageAccount"));
        assertTrue(partitionInfo.getProperties().containsKey("complianceRuleSet"));
        assertTrue(partitionInfo.getProperties().containsKey("id"));
    }

    @Test
    public void should_returnPartition_when_partitionExists() {
        when(this.tableStore.getPartition(PARTITION_ID)).thenReturn(properties);

        PartitionInfo partitionInfo = this.sut.getPartition(PARTITION_ID);

        assertTrue(partitionInfo.getProperties().containsKey("storageAccount"));
        assertTrue(partitionInfo.getProperties().containsKey("complianceRuleSet"));
        assertTrue(partitionInfo.getProperties().containsKey("id"));
    }

    @Test
    public void should_throwNotFoundException_when_partitionDoesNotExist() {
        when(this.tableStore.getPartition(PARTITION_ID)).thenReturn(new HashMap<>());

        try {
            sut.getPartition(PARTITION_ID);
        } catch (AppException e) {
            assertEquals(404, e.getError().getCode());
            assertTrue(e.getError().getReason().equalsIgnoreCase("partition not found"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("my-tenant partition not found"));
        }
    }

    @Test
    public void should_returnTrue_when_successfullyDeletingSecretes() {
        when(this.tableStore.partitionExists(PARTITION_ID)).thenReturn(true);

        assertTrue(this.sut.deletePartition(PARTITION_ID));
    }

    @Test
    public void should_throwException_when_deletingNonExistentPartition() {
        try {
            this.sut.deletePartition("test-partition");
        } catch (AppException ae) {
            assertEquals(404, ae.getError().getCode());
            assertEquals("test-partition partition not found", ae.getError().getMessage());
        }
    }

    @Test
    public void should_throwException_when_deletingInvalidPartition() {
        assertThrows(AppException.class, () -> sut.deletePartition(null));
    }

    @Test
    public void should_throwException_when_deletingSystemPartition() {
        when(azureConfig.isReservedPartition("system")).thenReturn(true);
        AppException exception = assertThrows(AppException.class, () -> sut.deletePartition("system"));

        assertEquals(HttpStatus.SC_FORBIDDEN, exception.getError().getCode());
    }

    @Test
    public void should_returnEmptyList_when_no_partitions() {
        when(this.tableStore.getAllPartitions()).thenReturn(new ArrayList<>());

        List<String> partitions = sut.getAllPartitions();

        assertNotNull(partitions);
        assertTrue(partitions.isEmpty());
    }

    @Test
    public void should_returnEmptyList_when_partitionsIsNull() {
        when(this.tableStore.getAllPartitions()).thenReturn(null);

        List<String> partitions = sut.getAllPartitions();

        assertNull(partitions);
    }

    @Test
    public void should_returnPartitionList_when_partitionsExistsInCache() {
        List<String> partitionsList = Arrays.asList("partition1", "partition2");
        when(this.partitionListCache.get("getAllPartitions")).thenReturn(partitionsList);

        List<String> partitions = sut.getAllPartitions();

        assertNotNull(partitions);
        assertEquals(partitionsList, partitions);
    }

    @Test
    public void should_returnPartitionList_when_partitionsExists() {
        List<String> partitionsList = Arrays.asList("partition1", "partition2");
        when(this.tableStore.getAllPartitions()).thenReturn(partitionsList);

        List<String> partitions = sut.getAllPartitions();

        assertNotNull(partitions);
        assertEquals(partitionsList, partitions);
    }

    @Test
    public void should_returnPartitionListExceptSystemPartition_when_partitionsExists() {
        List<String> partitionsList = Arrays.asList("partition1", "system");
        when(this.tableStore.getAllPartitions()).thenReturn(partitionsList);
        when(azureConfig.isReservedPartition("system")).thenReturn(true);
        when(azureConfig.isReservedPartition("partition1")).thenReturn(false);

        List<String> partitions = sut.getAllPartitions();

        assertNotNull(partitions);
        assertEquals(Collections.singletonList("partition1"), partitions);
    }
}
