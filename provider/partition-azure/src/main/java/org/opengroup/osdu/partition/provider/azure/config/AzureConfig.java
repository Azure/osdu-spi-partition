package org.opengroup.osdu.partition.provider.azure.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {
    @Value("${reserved_partition_name:system}")
    private String reservedPartition;

    @Bean
    public TelemetryClient telemetryClient() {
        return new TelemetryClient();
    }

    public Boolean isReservedPartition(String partitionName) {
        return reservedPartition.equalsIgnoreCase(partitionName);
    }
}
