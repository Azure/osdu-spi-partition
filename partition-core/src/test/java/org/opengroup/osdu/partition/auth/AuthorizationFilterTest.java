// Copyright 2017-2020, Schlumberger
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

package org.opengroup.osdu.partition.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.opengroup.osdu.partition.service.PartitionServiceRole.DOMAIN_ADMIN;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.partition.provider.interfaces.IAuthorizationService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthorizationFilterTest {

    @Mock
    private IAuthorizationService authorizationService;

    @Mock
    private DpsHeaders headers;

    @InjectMocks
    private AuthorizationFilter sut;

    @Test
    public void should_authenticateRequest_when_resourceIsRolesAllowedAnnotated() {
        when(this.authorizationService.isDomainAdminServiceAccount()).thenReturn(true);

        assertTrue(this.sut.hasPermissions());

        verify(headers).put(DpsHeaders.USER_AUTHORIZED_GROUP_NAME, DOMAIN_ADMIN);
    }

    @Test
    public void should_throwAppError_when_noAuthzProvided() {
        when(this.authorizationService.isDomainAdminServiceAccount()).thenReturn(false);

        assertFalse(this.sut.hasPermissions());

        verifyNoInteractions(headers);
    }
}
