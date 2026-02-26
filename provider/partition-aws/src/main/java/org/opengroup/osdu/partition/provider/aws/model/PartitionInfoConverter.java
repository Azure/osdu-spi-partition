/* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

public class PartitionInfoConverter implements AttributeConverter<PartitionInfo> {

    @Override
    public AttributeValue transformFrom(PartitionInfo partitionInfo) {
        if (partitionInfo == null || partitionInfo.getProperties() == null) {
            return AttributeValue.builder().nul(true).build();
        }
        
        Map<String, AttributeValue> propertiesMap = new HashMap<>();
        partitionInfo.getProperties().forEach((key, property) -> {
            Map<String, AttributeValue> propertyMap = new HashMap<>();
            propertyMap.put("value", AttributeValue.builder().s(String.valueOf(property.getValue())).build());
            propertyMap.put("sensitive", AttributeValue.builder().bool(property.isSensitive()).build());
            propertiesMap.put(key, AttributeValue.builder().m(propertyMap).build());
        });
        
        return AttributeValue.builder().m(propertiesMap).build();
    }

    @Override
    public PartitionInfo transformTo(AttributeValue attributeValue) {
        if (attributeValue == null || Boolean.TRUE.equals(attributeValue.nul()) || attributeValue.m() == null) {
            return null;
        }
        
        Map<String, Property> properties = new HashMap<>();
        attributeValue.m().forEach((key, value) -> {
            if (value.m() != null) {
                AttributeValue valueAttr = value.m().get("value");
                AttributeValue sensitiveAttr = value.m().get("sensitive");
                
                Property property = new Property();
                if (valueAttr != null && valueAttr.s() != null) {
                    property.setValue(valueAttr.s());
                }
                if (sensitiveAttr != null && sensitiveAttr.bool() != null) {
                    property.setSensitive(sensitiveAttr.bool());
                }
                properties.put(key, property);
            }
        });
        
        PartitionInfo partitionInfo = new PartitionInfo();
        partitionInfo.setProperties(properties);
        return partitionInfo;
    }

    @Override
    public EnhancedType<PartitionInfo> type() {
        return EnhancedType.of(PartitionInfo.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.M;
    }
}
