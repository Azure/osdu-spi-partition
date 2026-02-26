// Copyright 2017-2020, Schlumberger
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

package org.opengroup.osdu.partition.middleware;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PartitionFilterTest {

    @Mock
    private DpsHeaders headers;
    @Mock
    private JaxRsDpsLog logger;
    @InjectMocks
    private PartitionFilter partitionFilter;

    @Test
    public void shouldSetCorrectResponseHeaders() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("https://test.com"));
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Mockito.when(headers.getCorrelationId()).thenReturn("correlation-id-value");
        Mockito.when(httpServletRequest.getMethod()).thenReturn("POST");
        org.springframework.test.util.ReflectionTestUtils.setField(partitionFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Origin", "custom-domain");
        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Headers", "access-control-allow-origin, origin, content-type, accept, authorization, data-partition-id, correlation-id, appkey");
        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Credentials", "true");
        Mockito.verify(httpServletResponse).addHeader("X-Frame-Options", "DENY");
        Mockito.verify(httpServletResponse).addHeader("X-XSS-Protection", "1; mode=block");
        Mockito.verify(httpServletResponse).addHeader("X-Content-Type-Options", "nosniff");
        Mockito.verify(httpServletResponse).addHeader("Cache-Control", "private, max-age=300");
        Mockito.verify(httpServletResponse).addHeader("Content-Security-Policy", "default-src 'self'");
        Mockito.verify(httpServletResponse).addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        Mockito.verify(httpServletResponse).addHeader("Expires", "0");
        Mockito.verify(httpServletResponse).addHeader("correlation-id", "correlation-id-value");
        Mockito.verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
    }

    @Test
    public void redirectHttp() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://test.com"));
        FilterChain filterChain = mock(FilterChain.class);
        when(httpServletRequest.getMethod()).thenReturn("POST");
        org.springframework.test.util.ReflectionTestUtils.setField(partitionFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletResponse).setStatus(307);
    }

    @Test
    public void optionsHasOkStatus() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("https://test.com"));
        FilterChain filterChain = mock(FilterChain.class);
        when(httpServletRequest.getMethod()).thenReturn("OPTIONS");
        org.springframework.test.util.ReflectionTestUtils.setField(partitionFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletResponse).setStatus(200);
    }

    @Test
    public void httpLocalhost_isAllowed_noRedirect() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/anything"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        org.springframework.test.util.ReflectionTestUtils.setField(partitionFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");

        doAnswer(inv -> { ((HttpServletResponse) inv.getArgument(1)).setStatus(200); return null; })
                .when(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        //Positive assertions (chain continues, status is ok)
        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse).setStatus(200);

        //Negative assertions (no redirect)
        verify(httpServletResponse, never()).setStatus(307);
        verify(httpServletResponse, never()).addHeader(eq("location"), anyString());
    }

    @Test
    public void httpNonLocalhost_triggersRedirect() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://example.com/test"));
        when(httpServletRequest.getMethod()).thenReturn("GET");

        org.springframework.test.util.ReflectionTestUtils.setField(partitionFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        // Positive assertions (redirect occurred)
        verify(httpServletResponse).setStatus(307);
        verify(httpServletResponse).addHeader(eq("location"), eq("https://example.com/test"));

        // Negative assertion (chain did not continue)
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    public void httpSwagger_isAllowed_noRedirect() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://test.com/swagger/index.html"));
        when(httpServletRequest.getMethod()).thenReturn("GET");
        org.springframework.test.util.ReflectionTestUtils.setField(partitionFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");

        // simulate downstream setting 200
        doAnswer(inv -> {
            ((HttpServletResponse) inv.getArgument(1)).setStatus(200);
            return null;
        }).when(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse).setStatus(200);
    }

    @Test
    public void healthChecks_areAllowed_noLogging_andReturn200_forBothEndpoints() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        org.springframework.test.util.ReflectionTestUtils.setField(partitionFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");

        String[] healthUris = {
                "http://test.com/liveness_check",
                "http://test.com/readiness_check"
        };

        for (String uri : healthUris) {
            // reset mocks between iterations so verifications are clean
            reset(req, res, chain, logger);

            when(req.getRequestURL()).thenReturn(new StringBuffer(uri));
            when(req.getMethod()).thenReturn("GET");

            // downstream sets 200 so we can assert the positive outcome
            doAnswer(inv -> { ((HttpServletResponse) inv.getArgument(1)).setStatus(200); return null; })
                    .when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

            partitionFilter.doFilter(req, res, chain);

            // allowed path: chain proceeds and status 200 observed
            verify(chain).doFilter(req, res);
            verify(res).setStatus(200);

            // health checks are not logged
            verify(logger, never()).request(any());
        }
    }

    @Test
    public void nonHealthRequest_isLogged_andReturns200() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // Use HTTPS to exercise normal path (no redirect) and trigger logging
        when(req.getRequestURL()).thenReturn(new StringBuffer("https://test.com/data"));
        when(req.getMethod()).thenReturn("GET");

        org.springframework.test.util.ReflectionTestUtils.setField(
                partitionFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");

        doAnswer(inv -> { ((HttpServletResponse) inv.getArgument(1)).setStatus(200); return null; })
                .when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        partitionFilter.doFilter(req, res, chain);

        // ✅ normal requests are logged
        verify(chain).doFilter(req, res);
        verify(res).setStatus(200);
        verify(logger).request(any());
    }

    @Test
    public void xForwardedProto_https_header_allowsHttpUrl_andLogs200() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://test.com/path"));
        when(httpServletRequest.getHeader("x-forwarded-proto")).thenReturn("https");
        when(httpServletRequest.getMethod()).thenReturn("GET");
        org.springframework.test.util.ReflectionTestUtils.setField(partitionFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");

        // Simulate downstream success so we can assert the positive outcome explicitly
        doAnswer(inv -> { ((HttpServletResponse) inv.getArgument(1)).setStatus(200); return null; })
                .when(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        // Positive: request proceeded and final status is 200
        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse).setStatus(200);

        // Logging occurs for non-health requests
        verify(logger).request(Mockito.any());

        // Negative: no redirect
        verify(httpServletResponse, never()).setStatus(307);
        verify(httpServletResponse, never()).addHeader(eq("location"), anyString());
    }

    @Test
    public void acceptHttpFlag_true_allowsHttp_andLogs200() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://test.com/resource"));
        when(httpServletRequest.getMethod()).thenReturn("GET");
        org.springframework.test.util.ReflectionTestUtils.setField(partitionFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");
        org.springframework.test.util.ReflectionTestUtils.setField(partitionFilter, "acceptHttp", true);

        // simulate downstream handler setting 200
        doAnswer(inv -> { ((HttpServletResponse) inv.getArgument(1)).setStatus(200); return null; })
                .when(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        // Positive: chain proceeds and status 200 returned
        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        verify(httpServletResponse).setStatus(200);

        // Positive: normal requests are logged
        verify(logger).request(Mockito.any());

        // Negative: no redirect
        verify(httpServletResponse, never()).setStatus(307);
        verify(httpServletResponse, never()).addHeader(eq("location"), anyString());
    }

    @Test
    public void acceptHttpFlag_false_triggersRedirect307() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://test.com/resource"));
        when(httpServletRequest.getMethod()).thenReturn("GET");

        org.springframework.test.util.ReflectionTestUtils.setField(
                partitionFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");
        org.springframework.test.util.ReflectionTestUtils.setField(
                partitionFilter, "acceptHttp", false);  // explicitly block HTTP

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        // Positive: redirect occurred
        verify(httpServletResponse).setStatus(307);
        verify(httpServletResponse).addHeader(eq("location"), eq("https://test.com/resource"));

        // Negative: chain not called, no 200
        verify(filterChain, never()).doFilter(any(), any());
        verify(httpServletResponse, never()).setStatus(200);
    }
}
