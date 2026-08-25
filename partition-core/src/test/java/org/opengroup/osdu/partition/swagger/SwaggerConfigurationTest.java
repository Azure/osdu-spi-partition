// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.swagger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockServletContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SwaggerConfigurationTest {

    @InjectMocks
    private SwaggerConfiguration cfg;

    @Mock
    private SwaggerConfigurationProperties configurationProperties;

    @Test
    public void openApi_includesServer_whenFullUrlDisabled() {
        when(configurationProperties.getApiTitle()).thenReturn("Test API");
        when(configurationProperties.getApiDescription()).thenReturn("Test Description");
        when(configurationProperties.getApiVersion()).thenReturn("1.0.0");
        when(configurationProperties.getApiContactName()).thenReturn("Test Contact");
        when(configurationProperties.getApiContactEmail()).thenReturn("contact@test.com");
        when(configurationProperties.getApiLicenseName()).thenReturn("Test License");
        when(configurationProperties.getApiLicenseUrl()).thenReturn("http://test-license.com");
        when(configurationProperties.isApiServerFullUrlEnabled()).thenReturn(false);

        MockServletContext sc = new MockServletContext();
        sc.setContextPath("/test-path");
        var api = cfg.openApi(sc);

        assertEquals("/test-path", api.getServers().get(0).getUrl());
    }

    @Test
    public void openApi_excludesServer_whenFullUrlEnabled() {
        when(configurationProperties.isApiServerFullUrlEnabled()).thenReturn(true);
        var api = cfg.openApi(new MockServletContext());
        assertTrue(api.getServers() == null || api.getServers().isEmpty());
    }
}
