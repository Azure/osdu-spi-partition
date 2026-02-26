// Copyright 2017-2020, Schlumberger
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

import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;

import lombok.Setter;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.common.Validators;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import jakarta.inject.Named;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "azure.table-storage")
@Setter
public class TableStorageBootstrapConfig {
    private int retryDeltaBackoffMs;
    private int retryMaxAttempts;

    @Bean
    @Lazy
    public TableServiceClient getTableServiceClient(
        final @Named("TABLE_STORAGE_ENDPOINT") String storageAccountEndpoint) {
        try {
            
            Validators.checkNotNullAndNotEmpty(storageAccountEndpoint, "storageAccountEndpoint");
            
            //There was no substitute function available for setting the maximum execution time as in the previous
            //version after my research, leaving that part for now. We would still like to know the replaceable code for below line:
            //cloudTableClient.getDefaultRequestOptions().setMaximumExecutionTimeInMs(maximumExecutionTimeMs);
            com.azure.core.http.policy.FixedDelayOptions fixedDelayOptions = new FixedDelayOptions(retryMaxAttempts, Duration.ofMillis(retryDeltaBackoffMs));
            RetryOptions retryOptions = new RetryOptions(fixedDelayOptions);
            
            TokenCredential managedIdentityCredential = new DefaultAzureCredentialBuilder().build();

            TableServiceClient serviceClient = new TableServiceClientBuilder()
                    .endpoint(storageAccountEndpoint)
                    .credential(managedIdentityCredential)
                    .retryOptions(retryOptions)
                    .buildClient();

            return serviceClient;
        }
        catch (Exception e){
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error creating cloud table storage client", e.getMessage(), e);
        }
    }

    @Bean
    @Lazy
    public TableClient getTableClient(
            TableServiceClient tableServiceClient,
            final DataTableConfiguration tblConfiguration) {
        try {
            Validators.checkNotNull(tableServiceClient, "tableServiceClient");
            Validators.checkNotNull(tblConfiguration, "tblConfiguration");

            //Attempting to create the table first, since if the table is already existing we get a null tableClient.
            //The behaviour of the API when a table is existing was not clearly documented here
            //https://learn.microsoft.com/en-us/java/api/com.azure.data.tables.tableserviceclient?view=azure-java-stable#com-azure-data-tables-tableserviceclient-createtableifnotexists(java-lang-string).
            TableClient tableClient = tableServiceClient.createTableIfNotExists(tblConfiguration.getCloudTableName());
            if(tableClient == null){
                //On the other hand, if we attempt to getTableClient for a non-existent table, it would not give us null
                tableClient = tableServiceClient.getTableClient(tblConfiguration.getCloudTableName());
            }
            return tableClient;
        }
        catch (Exception e){
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, String.format("Error creating a Table Client for table: %s", tblConfiguration), e.getMessage(), e);
        }

    }
}
