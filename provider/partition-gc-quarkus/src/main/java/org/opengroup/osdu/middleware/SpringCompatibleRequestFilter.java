/*
 *  Copyright 2020-2025 Google LLC
 *  Copyright 2020-2025 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.middleware;

import static org.jboss.resteasy.reactive.RestResponse.Status.BAD_REQUEST;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.opengroup.osdu.model.exception.AppException;

@ApplicationScoped
// This class is designed to mimic Spring's behavior in request filtering.
public class SpringCompatibleRequestFilter {

  @ServerRequestFilter
  public void filterRequest(RoutingContext context) {
    if (context.request().path().contains("//")) {
      throw new AppException(
          BAD_REQUEST.getStatusCode(), BAD_REQUEST.getReasonPhrase(), "Bad request path");
    }
  }
}
