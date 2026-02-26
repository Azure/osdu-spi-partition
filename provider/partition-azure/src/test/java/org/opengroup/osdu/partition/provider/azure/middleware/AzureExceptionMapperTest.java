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

package org.opengroup.osdu.partition.provider.azure.middleware;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.google.gson.Gson;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AzureExceptionMapperTest {
    private static final Gson gson = new Gson();

    @Mock
    private JaxRsDpsLog logger;

    @Mock
    private TelemetryClient telemetryClient;

    @InjectMocks
    private AzureExceptionMapper sut;

    @Test
    public void should_use429ValueInResponse_When_TooManyRequestsExceptionIsHandledByAzureExceptionMapper() {
        HttpResponseException exception = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(exception.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS.value());
        when(exception.getMessage()).thenReturn("Too many reqeusts");
        when(exception.getLocalizedMessage()).thenReturn("Too many reqeusts");

        ResponseEntity<Object> response = sut.handleHttpResponseException(exception);

        assertEquals(429, response.getStatusCodeValue());
        assertEquals(gson.toJson("Too many reqeusts"), response.getBody());
    }

    @Test
    public void should_useGenericValuesInResponse_When_HttpResponseExceptionIsHandledByAzureExceptionMapper() {
        HttpResponseException exception = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(exception.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getStatusCode()).thenReturn(430);

        ResponseEntity<?> response = sut.handleHttpResponseException(exception);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals(gson.toJson("An unknown error has occurred."), response.getBody());
    }

    @Test
    public void should_getErrorResponseEntityWhenPassedAppException() {
        doNothing().when(telemetryClient).trackException(ArgumentMatchers.any(Exception.class));
        AppException appException = new AppException(404, "partition not found", "given partition not found");

        ResponseEntity<Object> responseEntity = sut.getErrorResponse(appException);

        assertEquals(404, responseEntity.getStatusCodeValue());
    }
}
