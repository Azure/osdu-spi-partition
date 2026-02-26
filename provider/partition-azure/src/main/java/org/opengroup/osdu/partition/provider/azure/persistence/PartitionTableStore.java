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
import com.azure.data.tables.models.TableTransactionAction;
import com.azure.data.tables.models.TableTransactionActionType;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PartitionTableStore {

    private final static String ID = "id";
    private final static String VALUE = "value";
    private final static String SENSITIVE = "sensitive";

    private final static String PARTITION_KEY = "PartitionKey";
    private final static String ROW_KEY = "RowKey";

    @Autowired
    private DataTableStore dataTableStore;

    public void addPartition(String partitionId, PartitionInfo partitionInfo) {

        Map<String, Property> requestProperties = partitionInfo.getProperties();
        requestProperties.put(ID, Property.builder().value(partitionId).build());

        //create a list of transactions required
        List<TableTransactionAction> actionsList = new ArrayList<>();
        for (Map.Entry<String, Property> entry : requestProperties.entrySet()) {
            String key = entry.getKey();
            Property property = entry.getValue();

            TableEntity partitionEntity = new TableEntity(partitionId, key);
            Map<String, Object> properties = new HashMap<>();

            if (property.isSensitive()) {
                property.setValue(this.getTenantSafeSecreteId(partitionId, String.valueOf(property.getValue())));
            }
            properties.put(VALUE, property.getValue());
            properties.put(SENSITIVE, property.isSensitive());
            partitionEntity.setProperties(properties);

            TableTransactionAction tableTransactionAction = new TableTransactionAction(TableTransactionActionType.UPSERT_MERGE,
                    partitionEntity);

            actionsList.add(tableTransactionAction);
        }

        this.dataTableStore.insertBatchEntities(actionsList);
    }

    public boolean partitionExists(String partitionId) {
        List<TableEntity> partitionEntities = this.queryById(partitionId);
        return partitionEntities.size() == 1;
    }

    public Map<String, Property> getPartition(String partitionId) {
        Map<String, Property> out = new HashMap<>();

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<TableEntity> partitionEntities = this.getAllByPartitionId(partitionId);
        stopwatch.stop();
        log.info(String.format("Total time taken by Get Partition Method based on PartitionId: %d", stopwatch.elapsed(TimeUnit.MILLISECONDS)));

        if (partitionEntities.isEmpty()) {
            return out;
        }

        for (TableEntity pe : partitionEntities) {
            Property property = Property.builder().build();
            Map<String, Object> properties = pe.getProperties();
            if (properties.containsKey(SENSITIVE)) {
                property.setSensitive((boolean) properties.get(SENSITIVE));
            }
            if (properties.containsKey(VALUE)) {
                property.setValue(properties.get(VALUE).toString());
            }
            out.put(pe.getRowKey(), property);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public void deletePartition(String partitionId) {
        Iterable<TableEntity> results = this.dataTableStore.queryByKey(PARTITION_KEY, partitionId);
        for (TableEntity tableEntity : results) {
            this.dataTableStore.deleteCloudTableEntity(tableEntity.getPartitionKey(), tableEntity.getRowKey());
        }
    }

    @SuppressWarnings("unchecked")
    private List<TableEntity> queryById(String partitionId) {
        List<TableEntity> out = new ArrayList<>();
        Iterable<TableEntity> results = this.dataTableStore.queryByCompoundKey(
                        ROW_KEY, ID,
                        VALUE, partitionId);
        for (TableEntity tableEntity : results) {
            out.add(tableEntity);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<TableEntity> getAllByPartitionId(String partitionId) {
        List<TableEntity> out = new ArrayList<>();
        Iterable<TableEntity> results = (Iterable<TableEntity>)
                this.dataTableStore.queryByKey(PARTITION_KEY, partitionId);
        results.forEach(out::add);
        log.info("Result Size {}", out.size());
        return out;
    }

    private String getTenantSafeSecreteId(String partitionId, String secreteName) {
        return String.format("%s-%s", partitionId, secreteName);
    }

    public List<String> getAllPartitions() {
        List<String> partitions = new ArrayList<>();
        Iterable<TableEntity> results = this.dataTableStore.queryByKey( ROW_KEY, ID);
        for (TableEntity tableEntity : results) {
            partitions.add(tableEntity.getPartitionKey());
        }
        return partitions;
    }
}
