/*
  Copyright 2002-2021 Google LLC
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

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.cache.VmCache;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.coreplus.config.PropertiesConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class VmCacheConfiguration {

  private final PropertiesConfiguration properties;

  @Bean(name = "partitionListCache")
  public VmCache<String, List<String>> partitionListCache() {
    return new VmCache<>(this.properties.getCacheExpiration() * 60,
        this.properties.getCacheMaxSize());
  }

  @ConfigurationProperties
  @Bean(name = "partitionServiceCache")
  public VmCache<String, PartitionInfo> partitionServiceCache() {
    return new VmCache<>(this.properties.getCacheExpiration() * 60,
        this.properties.getCacheMaxSize());
  }
}
