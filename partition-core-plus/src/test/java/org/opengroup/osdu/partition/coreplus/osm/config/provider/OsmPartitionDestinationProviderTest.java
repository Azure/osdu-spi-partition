/*
  Copyright © Microsoft Corporation
  Copyright 2002-2021 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package org.opengroup.osdu.partition.coreplus.osm.config.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.osm.core.model.Destination;
import org.opengroup.osdu.partition.coreplus.config.PropertiesConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OsmPartitionDestinationProviderTest {

    @Mock
    private PropertiesConfiguration config;

    @Test
    void getDestination_buildsFromConfigValues() {
        when(config.getDataPartitionId()).thenReturn("test-partition");
        when(config.getPartitionNamespace()).thenReturn("test-namespace");
        when(config.getPartitionPropertyKind()).thenReturn("test-kind");

        OsmPartitionDestinationProvider provider = new OsmPartitionDestinationProvider(config);

        Destination dest = provider.getDestination();

        assertEquals("test-partition", dest.getPartitionId());
        assertEquals("test-namespace", dest.getNamespace().getName());
        assertEquals("test-kind", dest.getKind().getName());
    }
}
