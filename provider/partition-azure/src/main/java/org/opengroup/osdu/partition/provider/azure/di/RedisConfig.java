/*
 * Copyright 2017-2025, Microsoft
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

package org.opengroup.osdu.partition.provider.azure.di;

import org.opengroup.osdu.azure.cache.RedisAzureCache;
import org.opengroup.osdu.azure.di.RedisAzureConfiguration;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.inject.Named;
import java.util.List;

@Configuration
public class RedisConfig {
    @Value("${redis.port}")
    private int port;

    @Value("${redis.expiration}")
    private int expiration;

    @Value("${redis.database}")
    private int database;

    @Value("${redis.connection.timeout}")
    private long connectionTimeout;

    @Value("${redis.command.timeout}")
    private int commandTimeout;

    @Value("${redis.principal.id:}")
    private String redisPrincipalId;

    @Value("${redis.hostname:#{null}}")
    private String redisHostname;

    @Value("${redis.backupHostname:#{null}}")
    private String redisBackupHostname;

    @Value("${azure.is.backup:false}")
    private boolean isBackup;

    @Bean
    public RedisAzureCache<PartitionInfo> partitionServiceCache() {
        return new RedisAzureCache<>(PartitionInfo.class, createRedisConfiguration());
    }

    @Bean
    public RedisAzureCache<List<String>> partitionListCache() {
        return (RedisAzureCache<List<String>>) new RedisAzureCache(List.class, createRedisConfiguration());
    }

    private RedisAzureConfiguration createRedisConfiguration() {
        return new RedisAzureConfiguration(
            database,
            expiration,
            port,
            connectionTimeout,
            commandTimeout,
            redisPrincipalId,
            isBackup ? redisBackupHostname : redisHostname);
    }
}
