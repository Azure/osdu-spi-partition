/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.provider.ibm.cache;

import java.util.List;

import org.opengroup.osdu.core.common.cache.VmCache;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VmCacheConfiguration {

    @Bean(name = "partitionListCache")
    public VmCache<String, List<String>> partitionListCache(@Value("${cache.expiration}") final int expiration,
                                                            @Value("${cache.maxSize}") final int maxSize) {
        return new VmCache<>(expiration * 60, maxSize);
    }

    @Bean(name ="partitionServiceCache")
    public VmCache<String, PartitionInfo> partitionServiceCache(@Value("${cache.expiration}") final int expiration,
                                                            @Value("${cache.maxSize}") final int maxSize) {
        return new VmCache<>(expiration * 60, maxSize);
    }
}
