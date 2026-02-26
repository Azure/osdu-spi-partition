/*
 * Copyright © Amazon Web Services
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

package org.opengroup.osdu.partition.provider.aws.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.aws.v2.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.v2.ssm.K8sParameterNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderConfigurationBagTest {

    @Test
    void constructor_ShouldLoadDynamoDBTableName() throws K8sParameterNotFoundException {
        String expectedRegion = "us-east-1";
        String expectedTableName = "test-table-name";

        try (MockedConstruction<K8sLocalParameterProvider> mock = Mockito.mockConstruction(
                K8sLocalParameterProvider.class,
                (provider, context) -> {
                    when(provider.getParameterAsString("DYNAMODB_TABLE_NAME")).thenReturn(expectedTableName);
                })) {

            ProviderConfigurationBag config = new ProviderConfigurationBag(expectedRegion);

            assertEquals(expectedRegion, config.amazonRegion);
            assertEquals(expectedTableName, config.dynamodbTableName);
        }
    }

    @Test
    void constructor_ShouldThrowException_WhenParameterNotFound() throws K8sParameterNotFoundException {
        try (MockedConstruction<K8sLocalParameterProvider> mock = Mockito.mockConstruction(
                K8sLocalParameterProvider.class,
                (provider, context) -> {
                    when(provider.getParameterAsString("DYNAMODB_TABLE_NAME"))
                            .thenThrow(new K8sParameterNotFoundException(false, "DYNAMODB_TABLE_NAME", "Parameter not found"));
                })) {

            assertThrows(K8sParameterNotFoundException.class,
                    () -> new ProviderConfigurationBag("us-east-1"));
        }
    }
}
