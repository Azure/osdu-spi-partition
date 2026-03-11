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

import org.opengroup.osdu.core.common.info.VersionInfoBuilder;
import org.opengroup.osdu.core.common.model.info.VersionInfo;
import org.opengroup.osdu.partition.api.InfoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class InfoController implements InfoApi {
    @Autowired
    private VersionInfoBuilder versionInfoBuilder;

    @Override
    public VersionInfo info() throws IOException {
        return versionInfoBuilder.buildVersionInfo();
    }
}
