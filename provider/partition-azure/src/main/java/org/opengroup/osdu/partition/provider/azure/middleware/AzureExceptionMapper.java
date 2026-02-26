package org.opengroup.osdu.partition.provider.azure.middleware;

import com.azure.core.exception.HttpResponseException;
import com.google.gson.Gson;
import com.microsoft.applicationinsights.TelemetryClient;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.middleware.GlobalExceptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@RestController
public class AzureExceptionMapper extends GlobalExceptionMapper {

    private static final Gson gson = new Gson();

    @Autowired
    private JaxRsDpsLog logger;

    @Autowired
    private TelemetryClient telemetryClient;

    @ExceptionHandler(HttpResponseException.class)
    protected ResponseEntity<Object> handleHttpResponseException(HttpResponseException e) {
        if (e.getResponse().getStatusCode() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return this.getErrorResponse(new AppException(e.getResponse().getStatusCode(), e.getLocalizedMessage(), e.getMessage(), e));
        }

        return this.getErrorResponse(
                new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server error.",
                        "An unknown error has occurred.", e));
    }

    @Override
    protected ResponseEntity<Object> getErrorResponse(AppException e) {

        String exceptionMsg = e.getError().getMessage();

        if (e.getError().getCode() > 499) {
            this.logger.error(exceptionMsg, e);
        } else {
            this.logger.warning(exceptionMsg, e);
        }

        if (e.getOriginalException() != null) {
            telemetryClient.trackException(e.getOriginalException());
        } else {
            telemetryClient.trackException(e);
        }
        return new ResponseEntity<>(gson.toJson(exceptionMsg), HttpStatus.resolve(e.getError().getCode()));
    }
}
