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

package org.opengroup.osdu.partition.provider.azure.persistence;

import com.azure.data.tables.models.TableEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PartitionTableStoreTest {

    @InjectMocks
    private PartitionTableStore sut;

    @Mock
    private DataTableStore dataTableStore;

    private static final String PARTITION_ID = "partitionId";
    private static final String PARTITION_KEY = "PartitionKey";
    private static final String ROW_KEY = "RowKey";

    @Test
    public void should_returnFalse_whenPartitionNotExists() {
        boolean exist = sut.partitionExists(PARTITION_ID);
        assertFalse(exist);
    }

    @Test
    public void should_returnTrue_whenPartitionExists() {
        Collection<TableEntity> list = new ArrayList<>();
        TableEntity tableEntity = new TableEntity(PARTITION_ID, ROW_KEY);
        list.add(tableEntity);
        when(this.dataTableStore.queryByCompoundKey(any(), any(), any(), any())).thenReturn(list);

        boolean exist = sut.partitionExists(PARTITION_ID);

        assertTrue(exist);
    }

    @Test
    public void should_get_partitionInfo() {
        Collection<TableEntity> list = new ArrayList<>();
        TableEntity tableEntity = new TableEntity(PARTITION_ID, ROW_KEY);
        list.add(tableEntity);
        when(dataTableStore.queryByKey(PARTITION_KEY, PARTITION_ID)).thenReturn(list);

        Map<String, Property> partition = sut.getPartition(PARTITION_ID);

        assertNotNull(partition);
        assertEquals(1, partition.size());
    }

    @Test
    public void should_get_partitionInfoWithProperties() {
        Collection<TableEntity> list = new ArrayList<>();
        TableEntity tableEntity = new TableEntity(PARTITION_ID, ROW_KEY);
        Map<String, Object> properties = new HashMap<>();
        properties.put("sensitive", true);
        properties.put("value", "shared");
        tableEntity.setProperties(properties);
        list.add(tableEntity);
        when(dataTableStore.queryByKey(PARTITION_KEY, PARTITION_ID)).thenReturn(list);

        Map<String, Property> partition = sut.getPartition(PARTITION_ID);

        assertNotNull(partition);
        assertEquals(1, partition.size());
    }

    @Test
    public void should_returnEmpty_when_partitionNotFound() {
        Map<String, Property> partition = sut.getPartition(PARTITION_ID);

        assertNotNull(partition);
        assertEquals(0, partition.size());
    }

    @Test
    public void should_addPartition_whenPartitionProvided() {
        PartitionInfo partitionInfo = new PartitionInfo();
        Map<String, Property> properties = new HashMap<>();
        properties.put("storageAccount", Property.builder()
                .value("storage-account")
                .sensitive(true).build());
        properties.put("complianceRuleSet", Property.builder()
                .value("compliance-rule-set")
                .sensitive(false).build());
        partitionInfo.setProperties(properties);

        sut.addPartition(PARTITION_ID, partitionInfo);

        verify(this.dataTableStore, times(1)).insertBatchEntities(any());
    }

    @Test
    public void should_returnException_whenNoPartitionInfo() {
        doThrow(new AppException(500, "Error", "error creating partition")).when(dataTableStore).insertBatchEntities(any());
        try {
            sut.addPartition(PARTITION_ID, new PartitionInfo());
            fail("Should not be here");
        } catch (AppException e) {
            assertEquals(500, e.getError().getCode());
            assertEquals("error creating partition", e.getError().getMessage());
        }
    }

    @Test
    public void should_getAll_partitions() {
        Collection<TableEntity> list = new ArrayList<>();
        TableEntity tableEntity = new TableEntity(PARTITION_ID, ROW_KEY);
        list.add(tableEntity);
        when(dataTableStore.queryByKey("RowKey", "id")).thenReturn(list);

        List<String> partitions = sut.getAllPartitions();

        assertNotNull(partitions);
        assertEquals(1, partitions.size());
    }

    @Test
    public void should_delete_partition() {
        Collection<TableEntity> list = new ArrayList<>();
        TableEntity tableEntity = new TableEntity(PARTITION_KEY, ROW_KEY);
        list.add(tableEntity);
        when(dataTableStore.queryByKey(PARTITION_KEY, PARTITION_ID)).thenReturn(list);

        sut.deletePartition(PARTITION_ID);

        verify(dataTableStore, times(1)).deleteCloudTableEntity(PARTITION_KEY, ROW_KEY);
    }
}
