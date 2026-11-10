/*
 * Copyright 2017-2026, The Open Group
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

package org.opengroup.osdu.partition.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opengroup.osdu.core.common.http.HttpConfiguration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Partition's HTTP mapper moved from os-core-common's Jackson 2 bean to its Jackson 3 bean. This compares both on
 * the service's own API models, so a Jackson 3 default change cannot silently alter the partition API contract.
 */
class JacksonContractParityTest {

    private final com.fasterxml.jackson.databind.ObjectMapper jackson2 = legacyMapper();
    private final JsonMapper jackson3 = new HttpConfiguration().jsonObjectMapper();

    // Same configuration as os-core-common 9.x HttpConfiguration.jsonObjectMapper(), the HTTP mapper on Boot 3.
    private static com.fasterxml.jackson.databind.ObjectMapper legacyMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setTimeZone(TimeZone.getDefault());
        return mapper;
    }

    private static Map<String, Property> sampleProperties() {
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("host", "es");
        nested.put("port", 9200);
        nested.put("missing", null);
        Map<String, Property> properties = new LinkedHashMap<>();
        properties.put("compliance-ruleset", Property.builder().value("shared").build());
        properties.put("elastic-endpoint", Property.builder().sensitive(true).value("ELASTIC_ENDPOINT").build());
        properties.put("int", Property.builder().value(42).build());
        properties.put("long", Property.builder().value(9_000_000_000L).build());
        properties.put("double", Property.builder().value(1.5).build());
        properties.put("bool", Property.builder().value(true).build());
        properties.put("list", Property.builder().value(Arrays.asList("a", 1, null)).build());
        properties.put("object", Property.builder().value(nested).build());
        properties.put("null-value", Property.builder().value(null).build());
        properties.put("null-property", null);
        return properties;
    }

    @Test
    void responsesSerializeTheSame() throws Exception {
        Map<String, Property> properties = sampleProperties();
        assertSameJson(jackson2.writeValueAsString(properties), jackson3.writeValueAsString(properties));

        PartitionInfo info = PartitionInfo.builder().properties(properties).build();
        assertSameJson(jackson2.writeValueAsString(info), jackson3.writeValueAsString(info));

        List<String> partitions = List.of("opendes", "osdu");
        assertEquals(jackson2.writeValueAsString(partitions), jackson3.writeValueAsString(partitions));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"properties\":{\"a\":{\"sensitive\":true,\"value\":\"x\"},\"b\":{\"value\":1}}}",
            "{\"properties\":{\"a\":{\"sensitive\":null,\"value\":\"x\"}}}",
            "{\"properties\":{\"a\":{\"value\":{\"n\":[1,2.5,true,null,\"s\"]}}}}",
            "{\"properties\":{\"a\":{\"value\":9000000000,\"unknown\":1}},\"unknown\":\"ignored\"}",
            "{\"properties\":{\"a\":{\"value\":12345678901234567890}}}",
            "{\"properties\":{}}",
            "{}"
    })
    void requestsDeserializeTheSame(String body) throws Exception {
        assertEquals(jackson2.readValue(body, PartitionInfo.class), jackson3.readValue(body, PartitionInfo.class));
    }

    @Test
    void patchBodiesDeserializeTheSame() throws Exception {
        String body = "{\"a\":{\"sensitive\":false,\"value\":[1,{\"b\":null}]}}";
        assertEquals(
                jackson2.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Property>>() {}),
                jackson3.readValue(body, new TypeReference<Map<String, Property>>() {}));
    }

    // Intentional Jackson 3 change: trailing content after the JSON document is now rejected (400 for clients).
    @Test
    void trailingContentIsRejected() throws Exception {
        String body = "{\"properties\":{}} {\"x\":1}";
        jackson2.readValue(body, PartitionInfo.class);
        assertThrows(JacksonException.class, () -> jackson3.readValue(body, PartitionInfo.class));
    }

    private void assertSameJson(String expected, String actual) throws Exception {
        assertEquals(jackson2.readTree(expected), jackson2.readTree(actual), actual);
    }
}
