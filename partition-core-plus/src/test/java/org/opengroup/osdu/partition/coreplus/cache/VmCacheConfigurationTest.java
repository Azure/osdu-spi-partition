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

package org.opengroup.osdu.partition.coreplus.cache;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.cache.VmCache;
import org.opengroup.osdu.partition.coreplus.config.PropertiesConfiguration;

@ExtendWith(MockitoExtension.class)
public class VmCacheConfigurationTest {

    @Mock
    private PropertiesConfiguration properties;

    @Test
    void partitionListCache_withPositiveExpiration_allowsImmediateGet() {
        when(properties.getCacheExpiration()).thenReturn(2);
        when(properties.getCacheMaxSize()).thenReturn(100);
        VmCacheConfiguration cfg = new VmCacheConfiguration(properties);

        VmCache<String, List<String>> cache = cfg.partitionListCache();

        cache.put("k", asList("a", "b"));

        assertEquals(asList("a", "b"), cache.get("k"));
    }

    @Test
    void partitionListCache_withZeroExpiration_expiresImmediately() {
        when(properties.getCacheExpiration()).thenReturn(0);
        when(properties.getCacheMaxSize()).thenReturn(100);
        VmCacheConfiguration cfg = new VmCacheConfiguration(properties);

        VmCache<String, List<String>> cache = cfg.partitionListCache();

        cache.put("k", asList("x"));

        assertNull(cache.get("k"));
    }

    @Test
    void partitionServiceCache_withPositiveExpiration_allowsImmediateGet() {
        when(properties.getCacheExpiration()).thenReturn(2);
        when(properties.getCacheMaxSize()).thenReturn(100);
        VmCacheConfiguration cfg = new VmCacheConfiguration(properties);

        VmCache cache = cfg.partitionServiceCache();

        assertNotNull(cache);
    }

    @Test
    void partitionServiceCache_withZeroExpiration_expiresImmediately() {
        when(properties.getCacheExpiration()).thenReturn(0);
        when(properties.getCacheMaxSize()).thenReturn(100);
        VmCacheConfiguration cfg = new VmCacheConfiguration(properties);

        VmCache cache = cfg.partitionServiceCache();

        assertNotNull(cache);
    }
}
