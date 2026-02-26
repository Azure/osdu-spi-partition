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

package org.opengroup.osdu.partition.provider.gcp.config;

import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.partition.coreplus.PartitionApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

/**
 * Configuration that will not load original PartitionController into context, allowing its override
 * in PartitionControllerV2
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties
@ConditionalOnProperty(
    name = SystemApiConfiguration.PARTITION_SYSTEM_TENANT_API,
    havingValue = "true")
@PropertySource({"classpath:swaggerV2.properties"})
@ComponentScan(
    basePackages = {"org.opengroup.osdu"},
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {PartitionApplication.class}),
      @ComponentScan.Filter(
          type = FilterType.REGEX,
          pattern = "org.opengroup.osdu.partition.controller.PartitionController")
    })
public class SystemApiConfiguration {

  /** The name of the property to enable the partition system tenant API. */
  public static final String PARTITION_SYSTEM_TENANT_API = "system.tenant.api";

  /** The identifier of the system partition. */
  private String systemPartitionId = "system";

  /** Indicates whether the system partition is listable and its resources are ready. */
  private boolean systemPartitionListableAndResourceReady = false;
}
