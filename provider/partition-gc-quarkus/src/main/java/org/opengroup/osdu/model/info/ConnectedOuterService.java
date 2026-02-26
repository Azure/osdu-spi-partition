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
package org.opengroup.osdu.model.info;

import lombok.Builder;
import lombok.Data;

/**
 * The node contains service-specific values for all outer services connected to OSDU service.
 * The value is optional - basic implementation contains an empty list.
 * To define outer services info for OSDU service
 * need to override <code>loadConnectedOuterServices</code> method.
 */
@Data
@Builder
public class ConnectedOuterService {
  private String name;
  private String version;
}
