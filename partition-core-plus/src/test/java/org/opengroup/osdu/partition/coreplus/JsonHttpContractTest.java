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

package org.opengroup.osdu.partition.coreplus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.http.HttpConfiguration;
import org.opengroup.osdu.partition.coreplus.security.SecurityConfig;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The service picks up os-core-common's Jackson 3 JsonMapper through component scanning. It must replace Boot's
 * default mapper for HTTP, otherwise null primitives become 400s and null property values appear in responses.
 */
@SpringBootTest(classes = JsonHttpContractTest.App.class, properties = "management.server.port=")
@AutoConfigureMockMvc
class JsonHttpContractTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void httpUsesCommonJsonMapper() throws Exception {
        mvc.perform(post("/probe").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"properties\":{\"b\":{\"sensitive\":null,\"value\":null},\"a\":{\"value\":1}},\"x\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"b\":{\"sensitive\":false},\"a\":{\"sensitive\":false,\"value\":1}}", true));
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({SecurityConfig.class, HttpConfiguration.class, App.ProbeController.class})
    static class App {
        @RestController
        static class ProbeController {
            @PostMapping(value = "/probe", produces = MediaType.APPLICATION_JSON_VALUE)
            Map<String, Property> echo(@RequestBody PartitionInfo info) {
                return info.getProperties();
            }
        }
    }
}
