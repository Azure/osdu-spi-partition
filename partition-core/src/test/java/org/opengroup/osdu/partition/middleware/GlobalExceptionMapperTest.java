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

import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.exception.NotFoundException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppError;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GlobalExceptionMapperTest {

    @Mock
    private JaxRsDpsLog log;

    @InjectMocks
    private GlobalExceptionMapper sut;

    @Test
    public void should_useValuesInAppExceptionInResponse_When_AppExceptionIsHandledByGlobalExceptionMapper() {
        AppException exception = new AppException(409, "any reason", "any message");

        ResponseEntity<Object> response = sut.handleAppException(exception);
        assertEquals(409, response.getStatusCode().value());
        assertEquals(exception.getError(), response.getBody());
    }

    @Test
    public void should_addLocationHeader_when_fromAppException() {
        AppException exception = new AppException(302, "any reason", "any message");

        ResponseEntity<Object> response = sut.handleAppException(exception);
        assertEquals(302, response.getStatusCode().value());
    }

    @Test
    public void should_useGenericResponse_when_exceptionIsThrownDuringMapping() {
        AppException exception = new AppException(302, "any reason", "any message");

        ResponseEntity<Object> response = sut.handleAppException(exception);
        assertEquals(302, response.getStatusCode().value());
        assertEquals(exception.getError(), response.getBody());
    }

    @Test
    public void should_use404ValueInResponse_When_NotFoundExceptionIsHandledByGlobalExceptionMapper() {
        NotFoundException exception = new NotFoundException("any message");

        ResponseEntity<Object> response = sut.handleNotFoundException(exception);
        assertEquals(404, response.getStatusCode().value());
        assertEquals(new AppError(404, "Resource not found.", "any message"), response.getBody());
    }

    @Test
    public void should_useBadRequestInResponse_When_JsonProcessingExceptionIsHandledByGlobalExceptionMapper() {
        JacksonException exception = new StreamReadException("any message");

        ResponseEntity<Object> response = sut.handleValidationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

    @Test
    public void should_useBadRequestInResponse_When_handleUnrecognizedPropertyExceptionIsHandledByGlobalExceptionMapper() {
        UnrecognizedPropertyException exception = mock(UnrecognizedPropertyException.class);

        ResponseEntity<Object> response = sut.handleValidationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

    @Test
    public void should_useBadRequestInResponse_When_handleAccessDeniedExceptionIsHandledByGlobalExceptionMapper() {
        AccessDeniedException exception = new AccessDeniedException("Access is denied.");

        ResponseEntity<Object> response = sut.handleAccessDeniedException(exception);
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    public void should_returnBadRequest_when_NotSupportedExceptionIsCaptured() {
        ValidationException diException = new ValidationException("my bad");

        ResponseEntity<?> response = sut.handleValidationException(diException);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

    @Test
    public void should_returnBadRequest_when_ConstraintViolationExceptionIsCaptured() {
        ConstraintViolationException exception = new ConstraintViolationException("my bad", new HashSet<>());

        ResponseEntity<?> response = sut.handleConstraintValidationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertEquals(new AppError(400, "Validation error.", "{\"errors\":[\"Invalid payload\"]}"), response.getBody());

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("violation");
        exception.getConstraintViolations().add(violation);

        ResponseEntity<?> response2 = sut.handleConstraintValidationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response2.getStatusCode().value());
        assertEquals(new AppError(400, "Validation error.", "{\"errors\":[\"violation\"]}"), response2.getBody());
    }

    @Test
    public void should_useGenericValuesInResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {
        Exception exception = new Exception("any message");

        ResponseEntity<?> response = sut.handleGeneralException(exception);
        AppError error = (AppError) response.getBody();
        assertEquals(500, response.getStatusCode().value());
        assertEquals(500, error.getCode());
        assertEquals("Server error.", error.getReason());
        assertEquals("An unknown error has occurred.", error.getMessage());
    }

    @Test
    public void should_use405ValueInResponse_When_HttpRequestMethodNotSupportedExceptionIsHandledByGlobalExceptionMapper() {
        HttpHeaders httpHeaders = mock(HttpHeaders.class);
        WebRequest webRequest = mock(WebRequest.class);
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("any message");

        ResponseEntity<Object> response = sut.handleHttpRequestMethodNotSupported(exception, httpHeaders, HttpStatus.METHOD_NOT_ALLOWED, webRequest);
        assertEquals(405, response.getStatusCode().value());
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
