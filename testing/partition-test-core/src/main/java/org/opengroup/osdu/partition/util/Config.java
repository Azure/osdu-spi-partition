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

public class Config {

    public String hostUrl;
    public String osduTenant;
    public String clientTenant;
    public String dataPartitionId;

    private static Config config = new Config();

    public static Config Instance() {
        String env = System.getProperty("ENVIRONMENT", System.getenv("ENVIRONMENT"));

        if (env == null || env.equalsIgnoreCase("LOCAL")) {
            config.hostUrl = "http://localhost:8080/";
            config.clientTenant = "common";
            config.osduTenant = "opendes";
        } else {
            //Note: PARTITION_BASE_URL has a '/' at the end of it
            config.hostUrl = System.getProperty("PARTITION_BASE_URL", System.getenv("PARTITION_BASE_URL"));
            config.clientTenant = System.getProperty("CLIENT_TENANT", System.getenv("CLIENT_TENANT"));
            config.osduTenant = System.getProperty("MY_TENANT", System.getenv("MY_TENANT"));
            config.dataPartitionId = System.getProperty("DATA_PARTITION_ID", System.getenv("DATA_PARTITION_ID"));
        }
        return config;
    }

    public boolean isLocalHost() {
        return hostUrl.contains("//localhost");
    }
}