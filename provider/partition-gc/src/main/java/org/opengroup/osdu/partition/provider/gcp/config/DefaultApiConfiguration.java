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

import static org.opengroup.osdu.partition.provider.gcp.config.SystemApiConfiguration.PARTITION_SYSTEM_TENANT_API;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration that loads default PartitionController, used when SystemPartitionApi is disabled.
 */
@Configuration
@ConditionalOnProperty(
    name = PARTITION_SYSTEM_TENANT_API,
    havingValue = "false",
    matchIfMissing = true)
@ComponentScan(basePackages = {"org.opengroup.osdu"})
public class DefaultApiConfiguration {}
