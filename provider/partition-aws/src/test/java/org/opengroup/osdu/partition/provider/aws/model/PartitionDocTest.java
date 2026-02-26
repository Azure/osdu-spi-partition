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

package org.opengroup.osdu.partition.provider.aws.model;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PartitionDocTest {

    @Test
    void create_ShouldCreatePartitionDocWithIdAndInfo() {
        String id = "test-partition";
        Map<String, Property> properties = new HashMap<>();
        properties.put("key", Property.builder().value("value").build());
        PartitionInfo info = PartitionInfo.builder().properties(properties).build();

        PartitionDoc doc = PartitionDoc.create(id, info);

        assertNotNull(doc);
        assertEquals(id, doc.getId());
        assertEquals(info, doc.getPartitionInfo());
    }

    @Test
    void builder_ShouldCreatePartitionDoc() {
        String id = "test-id";
        PartitionInfo info = PartitionInfo.builder().properties(new HashMap<>()).build();

        PartitionDoc doc = PartitionDoc.builder()
                .id(id)
                .partitionInfo(info)
                .build();

        assertEquals(id, doc.getId());
        assertEquals(info, doc.getPartitionInfo());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        PartitionDoc doc = new PartitionDoc();
        String id = "partition-123";
        PartitionInfo info = PartitionInfo.builder().properties(new HashMap<>()).build();

        doc.setId(id);
        doc.setPartitionInfo(info);

        assertEquals(id, doc.getId());
        assertEquals(info, doc.getPartitionInfo());
    }
}
