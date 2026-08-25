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

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.opengroup.osdu.core.test.auth.UserType;
import org.opengroup.osdu.core.test.base.BaseAcceptanceTests;
import org.opengroup.osdu.core.test.client.PartitionClient;
import org.opengroup.osdu.core.test.service.ServiceType;

/**
 * Base acceptance test class for the Partition service.
 * Initialises a {@link PartitionClient} and exposes the data partition ID from the environment.
 */
public abstract class BasePartitionAcceptanceTests extends BaseAcceptanceTests {
    
    protected PartitionClient partitionClient;

    protected BasePartitionAcceptanceTests() {
        super(List.of(UserType.PRIVILEGED_USER),
              List.of(ServiceType.PARTITION_V1, ServiceType.ENTITLEMENTS_V2));
    }

    @BeforeEach
    @Override
    protected void setup() throws Exception {
        partitionClient = new PartitionClient(stringHttpClient, UserType.PRIVILEGED_USER);
    }

    @AfterEach
    @Override
    protected void teardown() throws Exception {
    }
}
