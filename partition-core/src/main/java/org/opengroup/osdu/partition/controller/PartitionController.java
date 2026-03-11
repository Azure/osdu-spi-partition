/*
 * Copyright 2017-2025, The Open Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.controller;

import org.opengroup.osdu.partition.api.PartitionApi;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class PartitionController implements PartitionApi {
    @Autowired
    @Qualifier("partitionServiceImpl")
    private IPartitionService partitionService;

    @Autowired
    private AuditLogger auditLogger;

    @Override
    public ResponseEntity create(String partitionId, PartitionInfo partitionInfo) {
        this.partitionService.createPartition(partitionId, partitionInfo);
        URI partitionLocation = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand().toUri();
        this.auditLogger.createPartitionSuccess(Collections.singletonList(partitionId));
        return ResponseEntity.created(partitionLocation).build();
    }

    @Override
    public void patch(String partitionId, PartitionInfo partitionInfo) {
        this.partitionService.updatePartition(partitionId, partitionInfo);
        this.auditLogger.updatePartitionSecretSuccess(Collections.singletonList(partitionId));
    }

    @Override
    public ResponseEntity<Map<String, Property>> get(String partitionId) {
        PartitionInfo partitionInfo = this.partitionService.getPartition(partitionId);
        this.auditLogger.readPartitionSuccess(Collections.singletonList(partitionId));
        return ResponseEntity.ok(partitionInfo.getProperties());
    }

    @Override
    public ResponseEntity delete(String partitionId) {
        this.partitionService.deletePartition(partitionId);
        this.auditLogger.deletePartitionSuccess(Collections.singletonList(partitionId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public List<String> list() {
        List<String> partitions = this.partitionService.getAllPartitions();
        this.auditLogger.readListPartitionSuccess(
                Collections.singletonList(String.format("Partition list size = %s", partitions.size())));
        return partitions;
    }
}
