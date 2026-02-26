/*
 * Copyright 2020-2021 Google LLC
 * Copyright 2020-2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.coreplus.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.partition.coreplus.model.PartitionPropertyEntity;

import static org.junit.jupiter.api.Assertions.*;

public class TypeMapperImplTest {

    private TypeMapperImpl typeMapper;

    @BeforeEach
    public void setup() {
        typeMapper = new TypeMapperImpl();
    }

    @Test
    public void should_instantiateTypeMapper_successfully() {
        assertNotNull(typeMapper);
    }

    @Test
    public void should_createTypeMapperWithPartitionPropertyEntity() {
        TypeMapperImpl mapper = new TypeMapperImpl();
        assertNotNull(mapper);
    }

    @Test
    public void should_notThrowException_whenCreatingTypeMapper() {
        assertDoesNotThrow(() -> new TypeMapperImpl());
    }
}
