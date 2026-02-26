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
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PartitionInfoConverterTest {

    private final PartitionInfoConverter converter = new PartitionInfoConverter();

    @Test
    void transformFrom_ShouldConvertPartitionInfoToAttributeValue() {
        Map<String, Property> properties = new HashMap<>();
        properties.put("key1", Property.builder().value("value1").sensitive(false).build());
        PartitionInfo info = PartitionInfo.builder().properties(properties).build();

        AttributeValue result = converter.transformFrom(info);

        assertNotNull(result);
        assertNotNull(result.m());
        assertTrue(result.m().containsKey("key1"));
        assertEquals("value1", result.m().get("key1").m().get("value").s());
        assertFalse(result.m().get("key1").m().get("sensitive").bool());
    }

    @Test
    void transformFrom_ShouldReturnNullAttributeValue_WhenPartitionInfoIsNull() {
        AttributeValue result = converter.transformFrom(null);

        assertNotNull(result);
        assertTrue(result.nul());
    }

    @Test
    void transformTo_ShouldConvertAttributeValueToPartitionInfo() {
        Map<String, AttributeValue> propertyMap = new HashMap<>();
        propertyMap.put("value", AttributeValue.builder().s("value1").build());
        propertyMap.put("sensitive", AttributeValue.builder().bool(true).build());
        
        Map<String, AttributeValue> propertiesMap = new HashMap<>();
        propertiesMap.put("key1", AttributeValue.builder().m(propertyMap).build());
        
        AttributeValue attributeValue = AttributeValue.builder().m(propertiesMap).build();

        PartitionInfo result = converter.transformTo(attributeValue);

        assertNotNull(result);
        assertNotNull(result.getProperties());
        assertTrue(result.getProperties().containsKey("key1"));
        assertEquals("value1", result.getProperties().get("key1").getValue());
        assertTrue(result.getProperties().get("key1").isSensitive());
    }

    @Test
    void transformTo_ShouldReturnNull_WhenAttributeValueIsNull() {
        PartitionInfo result = converter.transformTo(null);

        assertNull(result);
    }

    @Test
    void transformTo_ShouldReturnNull_WhenAttributeValueIsNul() {
        AttributeValue attributeValue = AttributeValue.builder().nul(true).build();

        PartitionInfo result = converter.transformTo(attributeValue);

        assertNull(result);
    }

    @Test
    void transformTo_ShouldReturnNull_WhenStringIsEmpty() {
        AttributeValue attributeValue = AttributeValue.builder().m(new HashMap<>()).build();

        PartitionInfo result = converter.transformTo(attributeValue);

        assertNotNull(result);
        assertNotNull(result.getProperties());
        assertTrue(result.getProperties().isEmpty());
    }

    @Test
    void type_ShouldReturnPartitionInfoEnhancedType() {
        EnhancedType<PartitionInfo> type = converter.type();

        assertNotNull(type);
        assertEquals(PartitionInfo.class, type.rawClass());
    }

    @Test
    void attributeValueType_ShouldReturnStringType() {
        AttributeValueType type = converter.attributeValueType();

        assertEquals(AttributeValueType.M, type);
    }
}
