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

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.api.InfoApi;
import org.opengroup.osdu.model.info.VersionInfo;
import org.opengroup.osdu.service.VersionInfoBuilder;

@RequiredArgsConstructor
public class InfoController implements InfoApi {
  private final VersionInfoBuilder versionInfoBuilder;

  @Override
  public VersionInfo info() {
    return versionInfoBuilder.buildVersionInfo();
  }
}
