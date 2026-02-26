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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.exception.NotFoundException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.WebRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionMapperTest {

    private static final Gson gson = new Gson();

    @Mock
    private JaxRsDpsLog log;

    @InjectMocks
    private GlobalExceptionMapper sut;

    @Test
    public void should_useValuesInAppExceptionInResponse_When_AppExceptionIsHandledByGlobalExceptionMapper() {
        AppException exception = new AppException(409, "any reason", "any message");

        ResponseEntity<Object> response = sut.handleAppException(exception);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("\"any message\"", response.getBody());
    }

    @Test
    public void should_addLocationHeader_when_fromAppException() {
        AppException exception = new AppException(302, "any reason", "any message");

        ResponseEntity<Object> response = sut.handleAppException(exception);
        assertEquals(302, response.getStatusCodeValue());
    }

    @Test
    public void should_useGenericResponse_when_exceptionIsThrownDuringMapping() {
        AppException exception = new AppException(302, "any reason", "any message");

        ResponseEntity<Object> response = sut.handleAppException(exception);
        assertEquals(302, response.getStatusCodeValue());
        assertEquals(gson.toJson("any message"), response.getBody());
    }


    @Test
    public void should_use404ValueInResponse_When_NotFoundExceptionIsHandledByGlobalExceptionMapper() {
        NotFoundException exception = new NotFoundException("any message");

        ResponseEntity<Object> response = sut.handleNotFoundException(exception);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals(gson.toJson("any message"), response.getBody());
    }

    @Test
    public void should_useBadRequestInResponse_When_JsonProcessingExceptionIsHandledByGlobalExceptionMapper() {
        JsonProcessingException exception = new JsonParseException(null, "any message");

        ResponseEntity<Object> response = sut.handleValidationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
    }

    @Test
    public void should_useBadRequestInResponse_When_handleUnrecognizedPropertyExceptionIsHandledByGlobalExceptionMapper() {
        UnrecognizedPropertyException exception = mock(UnrecognizedPropertyException.class);

        ResponseEntity<Object> response = sut.handleValidationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
    }

    @Test
    public void should_useBadRequestInResponse_When_handleAccessDeniedExceptionIsHandledByGlobalExceptionMapper() {
        AccessDeniedException exception = new AccessDeniedException("Access is denied.");

        ResponseEntity<Object> response = sut.handleAccessDeniedException(exception);
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCodeValue());
    }

    @Test
    public void should_returnBadRequest_when_NotSupportedExceptionIsCaptured() {
        ValidationException diException = new ValidationException("my bad");

        ResponseEntity<?> response = sut.handleValidationException(diException);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
    }

    @Test
    public void should_returnBadRequest_when_ConstraintViolationExceptionIsCaptured() {
        ConstraintViolationException exception = new ConstraintViolationException("my bad", new HashSet<>());

        ResponseEntity<?> response = sut.handleConstraintValidationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("\"{\\\"errors\\\":[\\\"Invalid payload\\\"]}\"", response.getBody());

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("violation");
        exception.getConstraintViolations().add(violation);

        ResponseEntity<?> response2 = sut.handleConstraintValidationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response2.getStatusCodeValue());
        assertEquals("\"{\\\"errors\\\":[\\\"violation\\\"]}\"", response2.getBody());
    }

    @Test
    public void should_useGenericValuesInResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {
        Exception exception = new Exception("any message");

        ResponseEntity<?> response = sut.handleGeneralException(exception);
        assertEquals(500, response.getStatusCodeValue());
        assertEquals(gson.toJson("An unknown error has occurred."), response.getBody());
    }

    @Test
    public void should_use405ValueInResponse_When_HttpRequestMethodNotSupportedExceptionIsHandledByGlobalExceptionMapper() {
        HttpHeaders httpHeaders = mock(HttpHeaders.class);
        WebRequest webRequest = mock(WebRequest.class);
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("any message");

        ResponseEntity<Object> response = sut.handleHttpRequestMethodNotSupported(exception, httpHeaders, HttpStatus.METHOD_NOT_ALLOWED, webRequest);
        assertEquals(405, response.getStatusCodeValue());
    }

    @Test
    public void should_logErrorMessage_when_statusCodeLargerThan499() {
        Exception originalException = new Exception("any message");
        AppException appException = new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server error.",
                "An unknown error has occurred.", originalException);

        sut.handleAppException(appException);
        verify(this.log).error("An unknown error has occurred.", appException);
    }


    @Test
    public void should_logWarningMessage_when_statusCodeSmallerThan499() {
        NotFoundException originalException = new NotFoundException("any message");
        AppException appException = new AppException(HttpStatus.NOT_FOUND.value(),
                "Resource not found.", originalException.getMessage());

        this.sut.handleNotFoundException(originalException);
        verify(this.log).warning("any message", appException);
    }
}
