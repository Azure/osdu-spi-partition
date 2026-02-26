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

package org.opengroup.osdu.partition.provider.azure.utils;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.UserPrincipal;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthorizationServiceTest {

    @Mock
    private Authentication auth;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthorizationService authorizationService;

    @BeforeEach
    public void setup() {
        securityContext = Mockito.mock(SecurityContext.class);
        auth = Mockito.mock(Authentication.class);
    }

    private UserPrincipal createAADUserPrincipal(String claimName, String claimValue, String issuer) {
        final JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                //.subject("subject")
                .claim(claimName, claimValue)
                .issuer(issuer)
                .build();
        final JWSObject jwsObject = new JWSObject(new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                new Payload(jwtClaimsSet.toString()));
        return new UserPrincipal("token", jwsObject, jwtClaimsSet);
    }

    private DummyAuthToken createSAuthToken(final String email, final String appcode) {
        final Map<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("appcode", appcode);
        map.put("iss", "sauth-preview.slb.com");
        
        // Create a proper JWS token using the modern API
        javax.crypto.SecretKey key = Keys.hmacShaKeyFor("dummy-secret-key-that-is-long-enough".getBytes());
        
        String token = Jwts.builder()
            .claims(map)
            .issuer("sauth-preview.slb.com")
            .signWith(key)
            .compact();
        
        // Parse it back to get a proper Jws<Claims> object
        Jws<Claims> jws = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token);
        
        return new DummyAuthToken(jws);
    }

    private void createSAuthTokenSetSecurityContext(final String email, final String appcode) {
        DummyAuthToken dummyAuthToken = createSAuthToken(email, appcode);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(dummyAuthToken);
    }

    private UserPrincipal createAADUserPrincipalSetSecurityContext(String claimName, String claimValue, String issuer) {
        UserPrincipal dummyAADPrincipal = createAADUserPrincipal(claimName, claimValue, issuer);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(dummyAADPrincipal);
        return dummyAADPrincipal;
    }

    @Test
    public void shouldReturnFalseWhenSAuthTokenIsSetInContext() {
        createSAuthTokenSetSecurityContext("email", null);
        assertFalse(authorizationService.isDomainAdminServiceAccount());
    }

    @Test
    public void shouldReturnTrueWhenAADTokenIsSetInContext_AndIssuerIsAAD() {
        createAADUserPrincipalSetSecurityContext(TestUtils.APPID, TestUtils.getAppId(), TestUtils.getAadIssuer());
        assertTrue(authorizationService.isDomainAdminServiceAccount());
    }

    @Test
    public void shouldReturnTrueWhenAADTokenIsSetInContext_AndIssuerIsAADV2() {
        createAADUserPrincipalSetSecurityContext(TestUtils.APPID, TestUtils.getAppId(), TestUtils.getAadIssuerV2());
        assertTrue(authorizationService.isDomainAdminServiceAccount());
    }

    @Test
    public void shouldReturnFalseWhenAADTokenIsSetInContext_AndIssuerIsNotAAD() {
        createAADUserPrincipalSetSecurityContext(TestUtils.APPID, TestUtils.getAppId(), TestUtils.getNonAadIssuer());
        assertFalse(authorizationService.isDomainAdminServiceAccount());
    }

    @Test
    public void shouldReturnFalseWhenAADTokenIsSetInContext_AndUserTypeIsGuestUser() {
        createAADUserPrincipalSetSecurityContext("upn", "test-upn", TestUtils.getNonAadIssuer());
        assertFalse(authorizationService.isDomainAdminServiceAccount());
    }

    @Test
    public void shouldReturnFalseWhenAADTokenIsSetInContext_AndUserTypeIsRegularUser() {
        createAADUserPrincipalSetSecurityContext("unique_name", "test_unique_name", TestUtils.getNonAadIssuer());
        assertFalse(authorizationService.isDomainAdminServiceAccount());
    }

    @Getter
    public class DummyAuthToken {

        private final Jws<Claims> jws;

        public DummyAuthToken(Jws<Claims> jws) {
            this.jws = jws;
        }

        public <T> T getClaim(String claim, Class<T> type) {
            return jws.getPayload().get(claim, type);
        }

        public String getIssuer() {
            return jws.getPayload().getIssuer();
        }
    }
}
