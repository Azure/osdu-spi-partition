/*
  Copyright © Microsoft Corporation
  Copyright 2002-2021 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package org.opengroup.osdu.partition.coreplus.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class SecurityConfigTest {

    @Test
    void filterChain_configuresSecurityAndBuildsChain() throws Exception {

        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        SecurityFilterChain chain = mock(SecurityFilterChain.class);

        when(http.cors(any())).thenReturn(http);
        when(http.csrf(any())).thenReturn(http);
        when(http.sessionManagement(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.httpBasic(any())).thenReturn(http);

        new SecurityConfig().filterChain(http);

        verify(http).cors(any());
        verify(http).csrf(any());
        verify(http).sessionManagement(any());
        verify(http).authorizeHttpRequests(any());
        verify(http).httpBasic(any());
        verify(http).build();
        verifyNoMoreInteractions(http);
    }

    @Test
    void webSecurityCustomizer_ignoresExpectedPaths() {

        SecurityConfig config = new SecurityConfig();
        WebSecurityCustomizer customizer = config.webSecurityCustomizer();

        var web = mock(org.springframework.security.config.annotation.web.builders.WebSecurity.class);
        var ignored = mock(org.springframework.security.config.annotation.web.builders.WebSecurity.IgnoredRequestConfigurer.class);

        when(web.ignoring()).thenReturn(ignored);
        when(ignored.requestMatchers(any(String[].class))).thenReturn(ignored);

        customizer.customize(web);

        verify(web).ignoring();
        verify(ignored).requestMatchers("/api-docs", "/index", "/swagger");
        verifyNoMoreInteractions(web, ignored);
    }
}
