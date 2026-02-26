/*
 * Copyright 2020-2021 Google LLC
 * Copyright 2020-2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.coreplus.osm.config.provider;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.osm.core.model.Destination;
import org.opengroup.osdu.core.osm.core.model.Kind;
import org.opengroup.osdu.core.osm.core.model.Namespace;
import org.opengroup.osdu.partition.coreplus.config.PropertiesConfiguration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
@RequiredArgsConstructor
public class OsmPartitionDestinationProvider {

    private final PropertiesConfiguration config;

    public Destination getDestination() {
        return Destination.builder()
                .partitionId(config.getDataPartitionId())
                .namespace(new Namespace(config.getPartitionNamespace()))
                .kind(new Kind(config.getPartitionPropertyKind()))
                .build();
    }
}
