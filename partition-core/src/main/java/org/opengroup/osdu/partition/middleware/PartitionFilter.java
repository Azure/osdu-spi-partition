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

package org.opengroup.osdu.partition.middleware;

import org.opengroup.osdu.core.common.http.ResponseHeadersFactory;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@Lazy
public class PartitionFilter implements Filter {

    @Inject
    private DpsHeaders headers;
    @Inject
    private JaxRsDpsLog logger;
    @Value("${ACCEPT_HTTP:false}")
    private boolean acceptHttp;

    // defaults to * for any front-end, string must be comma-delimited if more than one domain
    @Value("${ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS:*}")
    String ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS;

    private ResponseHeadersFactory responseHeadersFactory = new ResponseHeadersFactory();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        headers.addCorrelationIdIfMissing();
        long startTime = System.currentTimeMillis();
        setResponseHeaders(httpServletResponse);

        try {
            if (!validateIsHttps(httpServletRequest, httpServletResponse)) {
                //do nothing
            } else if (httpServletRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
                httpServletResponse.setStatus(200);
            } else {
                chain.doFilter(request, response);
            }
        } finally {
            logRequest(httpServletRequest, httpServletResponse, startTime);
        }
    }

    private boolean validateIsHttps(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String uri = this.getUri(httpServletRequest);
        if (isLocalHost(uri) || isSwagger(uri) || isHealthCheck(uri)) {
            return true;
        }
        if (isAcceptHttp() || this.isHttps(httpServletRequest)) {
            return true;
        }

        String location = uri.replaceFirst("http", "https");
        httpServletResponse.setStatus(307);
        httpServletResponse.addHeader("location", location);
        return false;

    }

    private boolean isLocalHost(String uri) {
        return (uri.contains("//localhost") || uri.contains("//127.0.0.1"));
    }

    private boolean isHealthCheck(String uri) {
        return (uri.endsWith("/liveness_check") || uri.endsWith("/readiness_check"));
    }

    private boolean isSwagger(String uri) {
        return uri.contains("/swagger") || uri.contains("/v1/api-docs") || uri.contains("/configuration/ui") || uri.contains("/webjars/");
    }

    private void logRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse, long startTime) {
        String uri = this.getUri(servletRequest);
        if (!isHealthCheck(uri)) {
            this.logger.request(Request.builder()
                    .requestMethod(servletRequest.getMethod())
                    .latency(Duration.ofMillis(System.currentTimeMillis() - startTime))
                    .requestUrl(uri)
                    .Status(servletResponse.getStatus())
                    .ip(servletRequest.getRemoteAddr())
                    .build());
        }
    }

    private void setResponseHeaders(HttpServletResponse httpServletResponse) {
        Map<String, String> responseHeaders = responseHeadersFactory.getResponseHeaders(ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS);
        for(Map.Entry<String, String> header : responseHeaders.entrySet()){
            if("Cache-Control".equalsIgnoreCase(header.getKey())){
                httpServletResponse.addHeader(header.getKey(), "private, max-age=300");
            }else {
                httpServletResponse.addHeader(header.getKey(), header.getValue().toString());
            }
        }
        httpServletResponse.addHeader(DpsHeaders.CORRELATION_ID, this.headers.getCorrelationId());
    }

    private boolean isHttps(HttpServletRequest httpServletRequest) {
        return this.getUri(httpServletRequest).startsWith("https") || "https".equalsIgnoreCase(httpServletRequest.getHeader("x-forwarded-proto"));
    }

    private String getUri(HttpServletRequest httpServletRequest) {
        StringBuilder requestURL = new StringBuilder(httpServletRequest.getRequestURL().toString());
        String queryString = httpServletRequest.getQueryString();
        return queryString == null ? requestURL.toString() : requestURL.append('?').append(queryString).toString();
    }

    public boolean isAcceptHttp() {
        return acceptHttp;
    }
}
