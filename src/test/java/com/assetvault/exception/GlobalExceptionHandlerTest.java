package com.assetvault.exception;

import com.assetvault.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");

    @ParameterizedTest(name = "{0}")
    @MethodSource("notFoundExceptions")
    void notFoundExceptionsUse404(RuntimeException exception) {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(exception, request);

        assertError(response, HttpStatus.NOT_FOUND, exception.getMessage(), null);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("conflictExceptions")
    void conflictExceptionsUse409(RuntimeException exception) {
        ResponseEntity<ErrorResponse> response = handler.handleConflict(exception, request);

        assertError(response, HttpStatus.CONFLICT, exception.getMessage(), null);
    }

    @Test
    void licenseExpiredUses422() {
        LicenseExpiredException exception = new LicenseExpiredException("Cannot assign an expired software license");

        ResponseEntity<ErrorResponse> response = handler.handleLicenseExpired(exception, request);

        assertError(response, HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), null);
    }

    @Test
    void methodArgumentValidationReportsFieldErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "assetRequest");
        bindingResult.addError(new FieldError("assetRequest", "name", "must not be blank"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                methodParameter("validatedBody", 0),
                bindingResult
        );

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception, request);

        assertError(response, HttpStatus.BAD_REQUEST, "Request validation failed", "name");
        assertThat(response.getBody().fieldErrors()).containsEntry("name", "must not be blank");
    }

    @Test
    void constraintViolationsReportPathErrors() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("days");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be greater than or equal to 1");
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(exception, request);

        assertError(response, HttpStatus.BAD_REQUEST, "Request parameter validation failed", "days");
        assertThat(response.getBody().fieldErrors()).containsEntry("days", "must be greater than or equal to 1");
    }

    @Test
    void missingRequestParameterReportsFieldError() {
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException(
                "employeeId",
                "Long"
        );

        ResponseEntity<ErrorResponse> response = handler.handleMissingRequestParameter(exception, request);

        assertError(response, HttpStatus.BAD_REQUEST, "Request parameter validation failed", "employeeId");
        assertThat(response.getBody().fieldErrors()).containsEntry("employeeId", "must be provided");
    }

    @Test
    void servletRequestBindingUsesBadRequestWithOriginalMessage() {
        ServletRequestBindingException exception = new ServletRequestBindingException("Missing header");

        ResponseEntity<ErrorResponse> response = handler.handleServletRequestBinding(exception, request);

        assertError(response, HttpStatus.BAD_REQUEST, "Missing header", null);
    }

    @Test
    void malformedRequestBodiesUseStableMessage() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response = handler.handleMalformedRequestBody(exception, request);

        assertError(response, HttpStatus.BAD_REQUEST, "Malformed request body", null);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("badRequestExceptions")
    void badRequestExceptionsUse400(RuntimeException exception) {
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(exception, request);

        assertError(response, HttpStatus.BAD_REQUEST, exception.getMessage(), null);
    }

    @Test
    void dataIntegrityExceptionsUseStableConflictMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("duplicate key"),
                request
        );

        assertError(response, HttpStatus.CONFLICT, "Request conflicts with existing data", null);
    }

    @Test
    void unexpectedExceptionsHideInternalDetailsBehindStableMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new IllegalStateException("database password was bad"),
                request
        );

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", null);
    }

    static Stream<RuntimeException> notFoundExceptions() {
        return Stream.of(
                new AssetNotFoundException("asset missing"),
                new EmployeeNotFoundException("employee missing"),
                new AssignmentNotFoundException("assignment missing"),
                new MaintenanceRecordNotFoundException("maintenance missing"),
                new LicenseNotFoundException("license missing")
        );
    }

    static Stream<RuntimeException> conflictExceptions() {
        return Stream.of(
                new AssetNotAvailableException("asset unavailable"),
                new AssetRetiredException("asset retired"),
                new AssetAlreadyAssignedException("asset assigned"),
                new InactiveEmployeeException("inactive employee"),
                new NoLicenseSeatsAvailableException("no seats"),
                new LicenseAlreadyAssignedException("license assigned"),
                new EmployeeHasActiveAssetsException("active assets"),
                new DuplicateSerialNumberException("duplicate serial"),
                new DuplicateEmployeeException("duplicate employee"),
                new DuplicateLicenseException("duplicate license"),
                new MaintenanceStateException("invalid maintenance state")
        );
    }

    static Stream<RuntimeException> badRequestExceptions() throws Exception {
        return Stream.of(
                new IllegalArgumentException("from must be on or before to"),
                new MethodArgumentTypeMismatchException(
                        "BROKEN",
                        com.assetvault.model.enums.AssetStatus.class,
                        "status",
                        methodParameter("statusParam", 0),
                        new IllegalArgumentException("No enum constant")
                )
        );
    }

    private void assertError(
            ResponseEntity<ErrorResponse> response,
            HttpStatus status,
            String message,
            String expectedFieldError
    ) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.timestamp()).isNotNull();
        assertThat(body.status()).isEqualTo(status.value());
        assertThat(body.error()).isEqualTo(reason(status));
        assertThat(body.message()).isEqualTo(message);
        assertThat(body.path()).isEqualTo("/api/v1/test");
        if (expectedFieldError == null) {
            assertThat(body.fieldErrors()).isNull();
        } else {
            assertThat(body.fieldErrors()).containsKey(expectedFieldError);
        }
    }

    private static org.springframework.core.MethodParameter methodParameter(String methodName, int parameterIndex)
            throws NoSuchMethodException {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod(methodName, String.class);
        return new org.springframework.core.MethodParameter(method, parameterIndex);
    }

    @SuppressWarnings("unused")
    private void validatedBody(String body) {
    }

    @SuppressWarnings("unused")
    private void statusParam(String status) {
    }

    private String reason(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Bad Request";
            case NOT_FOUND -> "Not Found";
            case CONFLICT -> "Conflict";
            case UNPROCESSABLE_CONTENT -> "Unprocessable Entity";
            case INTERNAL_SERVER_ERROR -> "Internal Server Error";
            default -> status.name();
        };
    }
}
