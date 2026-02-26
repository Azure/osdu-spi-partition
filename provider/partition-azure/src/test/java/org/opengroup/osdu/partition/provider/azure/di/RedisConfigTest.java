// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.provider.azure.di;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.azure.cache.IRedisClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@SpringBootTest(classes = RedisConfig.class)
@TestPropertySource(properties = {
    "redis.port=6379",
    "redis.expiration=3600",
    "redis.database=0",
    "redis.connection.timeout=2000",
    "redis.command.timeout=2000",
    "redis.principal.id=test-client-id"
})
public class RedisConfigTest {

    @MockBean
    private IRedisClientFactory redisClientFactory;

    @Autowired
    private RedisConfig redisConfig;

    @Test
    public void testBothCachesAreIndependentBeans() {
        var partitionServiceCache = redisConfig.partitionServiceCache();
        var partitionListCache = redisConfig.partitionListCache();
        
        assertNotNull(partitionServiceCache, "partitionServiceCache should not be null");
        assertNotNull(partitionListCache, "partitionListCache should not be null");
        assertNotSame(
            partitionServiceCache, 
            partitionListCache,
            "partitionServiceCache and partitionListCache should be different bean instances"
        );
    }
}
