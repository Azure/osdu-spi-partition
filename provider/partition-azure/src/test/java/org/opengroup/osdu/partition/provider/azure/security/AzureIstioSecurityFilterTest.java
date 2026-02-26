package org.opengroup.osdu.partition.provider.azure.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AzureIstioSecurityFilterTest {
    private static final String X_ISTIO_CLAIMS_PAYLOAD = "x-payload";
    private static final String ISTIO_PAYLOAD = "{\"aud\":\"aud1\",\"iss\":\"https://iss1\",\"ver\":\"1.0\"}";
    private static final String INVALID_ISTIO_PAYLOAD = "\"aud\":\"aud1\",\"iss\":\"https://iss1\",\"ver\":\"1.0\"";

    @InjectMocks
    private AzureIstioSecurityFilter azureIstioSecurityFilter;

    @Test
    public void should_setCorrectAuthentication_when_istioPayloadExists() throws IOException, ServletException {
        ArgumentCaptor<PreAuthenticatedAuthenticationToken> authCaptor = ArgumentCaptor.forClass(PreAuthenticatedAuthenticationToken.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Enumeration<String> headerNames = Collections.enumeration(Arrays.asList("Content-Type", "header1"));
        SecurityContextHolder.setContext(securityContext);
        when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);
        when(httpServletRequest.getHeader(X_ISTIO_CLAIMS_PAYLOAD)).thenReturn(new String(Base64.getEncoder().encode(ISTIO_PAYLOAD.getBytes())));

        azureIstioSecurityFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(securityContext).setAuthentication(authCaptor.capture());
        PreAuthenticatedAuthenticationToken auth = authCaptor.getValue();
        assert_UserPrincipal((UserPrincipal) auth.getPrincipal());
        assertNull(auth.getCredentials());
        assertEquals(Collections.EMPTY_LIST, auth.getAuthorities());
        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
    }

    @Test
    public void should_setCorrectAuthentication_when_istioPayloadNotExists() throws IOException, ServletException {
        ArgumentCaptor<PreAuthenticatedAuthenticationToken> authCaptor = ArgumentCaptor.forClass(PreAuthenticatedAuthenticationToken.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Enumeration<String> headerNames = Collections.enumeration(Arrays.asList("Content-Type", "header1"));
        SecurityContextHolder.setContext(securityContext);
        when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);
        when(httpServletRequest.getHeader(X_ISTIO_CLAIMS_PAYLOAD)).thenReturn("");

        azureIstioSecurityFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(securityContext).setAuthentication(authCaptor.capture());
        PreAuthenticatedAuthenticationToken auth = authCaptor.getValue();
        assertNull(auth.getPrincipal());
        assertNull(auth.getCredentials());
        assertEquals(Collections.EMPTY_LIST, auth.getAuthorities());
        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
    }

    @Test
    public void should_throwAppException500_when_istioPayloadInvalidJsonData() throws IOException, ServletException {
        ArgumentCaptor<PreAuthenticatedAuthenticationToken> authCaptor = ArgumentCaptor.forClass(PreAuthenticatedAuthenticationToken.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Enumeration<String> headerNames = Collections.enumeration(Arrays.asList("Content-Type", "header1"));
        SecurityContextHolder.setContext(securityContext);
        when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);
        when(httpServletRequest.getHeader(X_ISTIO_CLAIMS_PAYLOAD)).thenReturn(new String(Base64.getEncoder().encode(INVALID_ISTIO_PAYLOAD.getBytes())));

        AppException appException = assertThrows(AppException.class,
                () -> azureIstioSecurityFilter.doFilter(httpServletRequest, httpServletResponse, filterChain));
        assertEquals(500, appException.getError().getCode());
        assertTrue(appException.getError().getMessage().contains("Invalid JSON"));

    }

    private void assert_UserPrincipal(UserPrincipal principal) {
        assertEquals(3, principal.getClaims().size());
        assertEquals(Arrays.asList("aud1"), principal.getClaims().get("aud"));
        assertEquals("https://iss1", principal.getClaims().get("iss"));
        assertEquals("1.0", principal.getClaims().get("ver"));
    }
}
