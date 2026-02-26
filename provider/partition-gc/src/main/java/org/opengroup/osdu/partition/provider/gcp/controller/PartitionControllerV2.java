/*
 * Copyright 2020-2024 Google LLC
 * Copyright 2020-2024 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.provider.gcp.controller;

import static org.opengroup.osdu.partition.provider.gcp.config.SystemApiConfiguration.PARTITION_SYSTEM_TENANT_API;

import java.util.List;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.controller.PartitionController;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.gcp.config.SystemApiConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = PARTITION_SYSTEM_TENANT_API, havingValue = "true")
public class PartitionControllerV2 extends PartitionController {

  private static final String NOT_ALLOWED = "Not allowed.";
  private static final String SYSTEM_TENANT_ERROR_MESSAGE = "The system tenant should be managed via system tenant API.";
  @Autowired
  private SystemApiConfiguration properties;

  @Override
  public ResponseEntity create(String partitionId, PartitionInfo partitionInfo) {
    if (partitionId.equalsIgnoreCase(properties.getSystemPartitionId())) {
      throw new AppException(HttpStatus.SC_FORBIDDEN, NOT_ALLOWED, SYSTEM_TENANT_ERROR_MESSAGE);
    }
    return super.create(partitionId, partitionInfo);
  }

  @Override
  public void patch(String partitionId, PartitionInfo partitionInfo) {
    if (partitionId.equalsIgnoreCase(properties.getSystemPartitionId())) {
      throw new AppException(HttpStatus.SC_FORBIDDEN, NOT_ALLOWED, SYSTEM_TENANT_ERROR_MESSAGE);
    }
    super.patch(partitionId, partitionInfo);
  }

  @Override
  public ResponseEntity delete(String partitionId) {
    if (partitionId.equalsIgnoreCase(properties.getSystemPartitionId())) {
      throw new AppException(HttpStatus.SC_FORBIDDEN, NOT_ALLOWED, SYSTEM_TENANT_ERROR_MESSAGE);
    }
    return super.delete(partitionId);
  }

  @Override
  public List<String> list() {
    List<String> partitions = super.list();
    if (properties.isSystemPartitionListableAndResourceReady()) {
      return partitions;
    } else {
      return partitions.stream()
          .filter(partition -> !partition.equalsIgnoreCase(properties.getSystemPartitionId()))
          .toList();
    }
  }
}
