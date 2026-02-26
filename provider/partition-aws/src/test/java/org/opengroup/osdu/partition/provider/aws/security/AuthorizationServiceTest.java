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

package org.opengroup.osdu.partition.provider.aws.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.aws.v2.entitlements.RequestKeys;
import org.opengroup.osdu.core.aws.v2.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.v2.ssm.K8sParameterNotFoundException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private DpsHeaders headers;

    @Mock
    private JaxRsDpsLog logger;

    private final String spuEmail = "spu@email.com";

    @InjectMocks
    private AuthorizationService authorizationService;

    private final Map<String, String> validHeaders = new HashMap<>();
    private final Map<String, String> validLowerCaseHeaders = new HashMap<>();
    private final Map<String, String> noTokenHeaders = new HashMap<>();

    @BeforeEach
    void setupGlobal() {
        ReflectionTestUtils.setField(authorizationService, "headers", headers);
        ReflectionTestUtils.setField(authorizationService, "logger", logger);

        validHeaders.put(RequestKeys.AUTHORIZATION_HEADER_KEY, spuEmail);
        validLowerCaseHeaders.put(RequestKeys.AUTHORIZATION_HEADER_KEY.toLowerCase(), spuEmail);
        authorizationService.spuEmail = spuEmail;
    }

    private String createJwt(String sub, String email) throws Exception {
        JWSSigner signer = new MACSigner("test-secret-key-must-be-at-least-256-bits");
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder().subject(sub);
        if (email != null) {
            builder.claim("email", email);
        }
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), builder.build());
        signedJWT.sign(signer);
        return "Bearer " + signedJWT.serialize();
    }

    @Test
    void should_return_when_initCalled() throws K8sParameterNotFoundException {

        try (MockedConstruction<K8sLocalParameterProvider> k8sParameterProvider = Mockito.mockConstruction(K8sLocalParameterProvider.class,
                (mock, context) -> { when(mock.getParameterAsString(anyString())).thenReturn(spuEmail); })) {
            authorizationService.init();

            assertEquals(spuEmail, authorizationService.spuEmail);
        }
    }

    @Test
    void should_returnTrue_when_isDomainAdminServiceAccountCalledWithRightHeaders() {
        when(headers.getHeaders()).thenReturn(validHeaders);
        when(headers.getUserId()).thenReturn(spuEmail);
        System.out.println("should_returnTrue_when_isDomainAdminServiceAccountCalledWithRightHeaders: " + headers.getUserId() + ", " + authorizationService.spuEmail);

        try {
            assertTrue(authorizationService.isDomainAdminServiceAccount());
        } catch (AppException exception) {
            System.out.println("should_returnTrue_when_isDomainAdminServiceAccountCalledWithRightHeaders Error: " + exception.getError().getDebuggingInfo());
            System.out.flush();
            throw exception;
        }
    }

    @Test
    void should_returnTrue_when_isDomainAdminServiceAccountCalledLowerCaseHeaders() {
        when(headers.getHeaders()).thenReturn(validLowerCaseHeaders);
        when(headers.getUserId()).thenReturn(spuEmail);

        System.out.println("should_returnTrue_when_isDomainAdminServiceAccountCalledLowerCaseHeaders: " + headers.getUserId() + ", " + authorizationService.spuEmail);

        try {
            assertTrue(authorizationService.isDomainAdminServiceAccount());
        } catch (AppException exception) {
            System.out.println("should_returnTrue_when_isDomainAdminServiceAccountCalledLowerCaseHeaders Error: " + exception.getError().getDebuggingInfo());
            System.out.flush();
            throw exception;
        }
    }

    @Test
    void should_ThrowAppException_when_isDomainAdminServiceAccountCalledWithInvalidHeaders() {
        when(headers.getHeaders()).thenReturn(validHeaders);
        String nonSpuEmail = "not-the-spu@email.com";
        when(headers.getUserId()).thenReturn(nonSpuEmail);

        AppException exception = assertThrows(AppException.class, () -> 
            authorizationService.isDomainAdminServiceAccount()
        );
        assertEquals(401, exception.getError().getCode());
        assertTrue(exception.getError().getReason().equalsIgnoreCase("Unauthorized"));
        assertTrue(exception.getError().getMessage().equalsIgnoreCase("The user is not authorized to perform this action"));
    }

    @Test
    void should_ThrowAppException_when_isDomainAdminServiceAccountCalledWithoutAuthHeaders() {
        when(headers.getHeaders()).thenReturn(noTokenHeaders);

        AppException exception = assertThrows(AppException.class, () -> 
            authorizationService.isDomainAdminServiceAccount()
        );
        assertEquals(401, exception.getError().getCode());
        assertTrue(exception.getError().getReason().equalsIgnoreCase("Unauthorized"));
        assertTrue(exception.getError().getMessage().equalsIgnoreCase("The user is not authorized to perform this action"));
    }

    @Test
    void should_ThrowAppException_when_isDomainAdminServiceAccountCalledWithUnauthUser() {
        when(headers.getHeaders()).thenReturn(validHeaders);
        when(headers.getUserId()).thenReturn(null);

        AppException exception = assertThrows(AppException.class, () -> 
            authorizationService.isDomainAdminServiceAccount()
        );
        assertEquals(401, exception.getError().getCode());
        assertTrue(exception.getError().getReason().equalsIgnoreCase("Unauthorized"));
        assertTrue(exception.getError().getMessage().equalsIgnoreCase("The user is not authorized to perform this action"));
    }

    @Test
    void should_ThrowAppException_when_isDomainAdminServiceAccountHasInternalError() {
        doThrow(RuntimeException.class).when(headers).getHeaders();

        AppException exception = assertThrows(AppException.class, () -> 
            authorizationService.isDomainAdminServiceAccount()
        );
        assertEquals(500, exception.getError().getCode());
        assertTrue(exception.getError().getReason().equalsIgnoreCase("Authentication Failure"));
    }

}
