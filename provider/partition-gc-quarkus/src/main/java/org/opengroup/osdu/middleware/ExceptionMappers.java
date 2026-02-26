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

import static org.jboss.resteasy.reactive.RestResponse.StatusCode.INTERNAL_SERVER_ERROR;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.NOT_FOUND;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.opengroup.osdu.model.exception.AppException;

public class ExceptionMappers {
  private static final Logger log = Logger.getLogger(ExceptionMappers.class);

  @ServerExceptionMapper
  public Response mapAppException(AppException e) {
    return getErrorResponse(e);
  }

  @ServerExceptionMapper
  public Response mapNotFoundException(NotFoundException e) {
    AppException appException =
        new AppException(
            NOT_FOUND, RestResponse.Status.NOT_FOUND.getReasonPhrase(), e.getMessage(), e);
    return getErrorResponse(appException);
  }

  @ServerExceptionMapper
  public Response mapException(Exception e) {
    AppException appException =
        new AppException(INTERNAL_SERVER_ERROR, "Server error", "An unknown error has occurred", e);
    return getErrorResponse(appException);
  }

  protected Response getErrorResponse(AppException e) {
    int statusCode = e.getError().getCode();
    String exceptionMsg = e.getError().getMessage();
    if (statusCode < 500) {
      log.warn(exceptionMsg, e.getOriginalException());
    } else {
      log.error(exceptionMsg, e.getOriginalException());
    }
    return Response.status(statusCode).entity(e.getError()).build();
  }
}
