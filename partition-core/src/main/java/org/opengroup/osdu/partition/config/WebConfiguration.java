/*
Copyright 2017-2024 SLB

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

package org.opengroup.osdu.partition.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.UrlHandlerFilter;

/**
 * Spring Framework 7 removed trailing slash path matching; this filter keeps "/path/" and "/path"
 * mapped to the same handler. It runs right after the Spring Security filter chain so that the
 * security firewall still sees, and rejects with 400, the original path (e.g. "partitions//").
 */
@Configuration
public class WebConfiguration {

    // Spring Boot's default Spring Security filter order (SecurityFilterProperties.DEFAULT_FILTER_ORDER)
    static final int SECURITY_FILTER_ORDER = -100;

    @Bean
    public FilterRegistrationBean<UrlHandlerFilter> trailingSlashFilter() {
        FilterRegistrationBean<UrlHandlerFilter> registration = new FilterRegistrationBean<>(
                UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build());
        registration.setOrder(SECURITY_FILTER_ORDER + 1);
        return registration;
    }
}
