/*
 *  Copyright 2020-2025 Google LLC
 *  Copyright 2020-2025 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.controller;

import jakarta.ws.rs.Path;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.annotations.Param;
import org.opengroup.osdu.api.PartitionApi;
import org.opengroup.osdu.model.Property;
import org.opengroup.osdu.service.IPartitionService;

@Path("/partitions")
@RequiredArgsConstructor
public class PartitionController implements PartitionApi {
  private final IPartitionService partitionService;

  @Override
  public List<String> list() {
    return partitionService.getPartitionList();
  }

  @Override
  public Map<String, Property> get(@Param String partitionId) {
    return partitionService.getPartition(partitionId).getProperties();
  }
}
