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

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

import java.util.Collections;
import java.util.HashMap;
import org.opengroup.osdu.core.osm.core.persistence.IdentityTranslator;
import org.opengroup.osdu.core.osm.core.translate.Instrumentation;
import org.opengroup.osdu.core.osm.core.translate.TypeMapper;
import org.opengroup.osdu.partition.coreplus.model.PartitionPropertyEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(SCOPE_SINGLETON)
public class TypeMapperImpl extends TypeMapper {
    public TypeMapperImpl(){
        super(java.util.List.of(
                new Instrumentation<>(PartitionPropertyEntity.class,
                        new HashMap<String, String>() {{
                            put("partitionId", "partition_id");
                        }},
                        java.util.Map.of(),
                        new IdentityTranslator<>(
                                PartitionPropertyEntity::getId,
                                (p, o) -> p.setId((String) o)
                        ),
                        Collections.singletonList("id")
                ))
        );
    }
}
