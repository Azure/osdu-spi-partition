package org.opengroup.osdu.partition.provider.azure.di;

import org.opengroup.osdu.core.common.cache.VmCache;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class VmConfig {

    @Bean
    @ConditionalOnProperty(value = "cache.provider", havingValue = "vm", matchIfMissing = true)
    public VmCache<String, List<String>> partitionListCache(@Value("${cache.expiration}") final int expiration,
                                                            @Value("${cache.maxSize}") final int maxSize) {
        return new VmCache<>(expiration * 60, maxSize);
    }

    @Bean
    @ConditionalOnProperty(value = "cache.provider", havingValue = "vm", matchIfMissing = true)
    public VmCache<String, PartitionInfo> partitionServiceCache(@Value("${cache.expiration}") final int expiration,
                                                                @Value("${cache.maxSize}") final int maxSize) {
        return new VmCache<>(expiration * 60, maxSize);
    }
}
