/*
 * Copyright 2017-2020, Schlumberger
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

package org.opengroup.osdu.partition.util;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import com.google.common.base.Strings;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;

public class AzureTestUtils extends TestUtils {

    @Override
    public synchronized String getAccessToken() throws Exception {
        String bearerToken = System.getProperty("INTEGRATION_TESTER_ACCESS_TOKEN", System.getenv("INTEGRATION_TESTER_ACCESS_TOKEN"));
        if(!Strings.isNullOrEmpty(bearerToken) && Strings.isNullOrEmpty(token)) {
            System.out.println("Using INTEGRATION_TESTER_ACCESS_TOKEN bearer token from environment variable");
            token = bearerToken;
        }
        else if (Strings.isNullOrEmpty(token)) {       
            System.out.println("Generating INTEGRATION_TESTER_ACCESS_TOKEN bearer token using SPN client id and secret");
            String sp_id = System.getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
            String sp_secret = System.getProperty("AZURE_TESTER_SERVICEPRINCIPAL_SECRET", System.getenv("AZURE_TESTER_SERVICEPRINCIPAL_SECRET"));
            String app_resource_id = System.getProperty("AZURE_AD_APP_RESOURCE_ID", System.getenv("AZURE_AD_APP_RESOURCE_ID"));
            String tenant_id = System.getProperty("AZURE_AD_TENANT_ID", System.getenv("AZURE_AD_TENANT_ID"));

            token = AzureServicePrincipal.getIdToken(sp_id, sp_secret, tenant_id, app_resource_id);
        }
        return "Bearer " + token;
    }

    @Override
    public synchronized String getNoAccessToken() throws Exception {
        String bearerToken = System.getProperty("NO_DATA_ACCESS_TESTER_ACCESS_TOKEN", System.getenv("NO_DATA_ACCESS_TESTER_ACCESS_TOKEN"));
        if(!Strings.isNullOrEmpty(bearerToken) && Strings.isNullOrEmpty(noAccessToken)) {
            System.out.println("Using NO_DATA_ACCESS_TESTER_ACCESS_TOKEN bearer token from environment variable");
            noAccessToken = bearerToken;
        }
        else if (Strings.isNullOrEmpty(noAccessToken)) {       
            System.out.println("Generating NO_DATA_ACCESS_TESTER_ACCESS_TOKEN bearer token using SPN client id and secret");
            String sp_id = System.getProperty("NO_DATA_ACCESS_TESTER", System.getenv("NO_DATA_ACCESS_TESTER"));
            String sp_secret = System.getProperty("NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET", System.getenv("NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET"));
            String app_resource_id = System.getProperty("AZURE_AD_OTHER_APP_RESOURCE_ID", System.getenv("AZURE_AD_OTHER_APP_RESOURCE_ID"));
            String tenant_id = System.getProperty("AZURE_AD_TENANT_ID", System.getenv("AZURE_AD_TENANT_ID"));
            noAccessToken = AzureServicePrincipal.getIdToken(sp_id, sp_secret, tenant_id, app_resource_id);
        }
        return "Bearer " + noAccessToken;
    }
}