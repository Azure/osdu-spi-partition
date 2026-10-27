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

package org.opengroup.osdu.partition.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opengroup.osdu.partition.di.BeanConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Runs on a real servlet container: MockMvc does not reproduce the firewall's handling of "//".
 */
@SpringBootTest(classes = TrailingSlashFilterOrderTest.App.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrailingSlashFilterOrderTest {

    @LocalServerPort
    private int port;

    @Configuration
    @EnableAutoConfiguration
    @Import({BeanConfiguration.class, WebConfiguration.class})
    static class App {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll()).build();
        }

        @RestController
        static class ProbeController {
            @GetMapping("/partitions")
            String list() {
                return "list";
            }

            @GetMapping("/partitions/{id}")
            String get() {
                return "get";
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
            "/partitions, 200",
            "/partitions/, 200",
            "/partitions/opendes/, 200",
            "/partitions//, 400",
            "/partitions/opendes//, 400"
    })
    void trailingSlashIsAcceptedButDoubleSlashIsRejectedByFirewall(String path, int expectedStatus) throws Exception {
        HttpResponse<Void> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build(),
                HttpResponse.BodyHandlers.discarding());
        assertEquals(expectedStatus, response.statusCode());
    }
}
