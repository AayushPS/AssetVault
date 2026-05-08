package com.assetvault.exception;

import com.assetvault.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps validation and domain exceptions to consistent HTTP error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Builds an error response for the supplied exception scenario.
     *
     * @param ex the captured exception
     * @param request the request payload
     * @return the resulting global exception handler
     */
    @ExceptionHandler({
            AssetNotFoundException.class,
            EmployeeNotFoundException.class,
            AssignmentNotFoundException.class,
            MaintenanceRecordNotFoundException.class,
            LicenseNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request, null, ex);
    }

    /**
     * Builds an error response for the supplied exception scenario.
     *
     * @param ex the captured exception
     * @param request the request payload
     * @return the resulting global exception handler
     */
    @ExceptionHandler({
            AssetNotAvailableException.class,
            AssetRetiredException.class,
            AssetAlreadyAssignedException.class,
            InactiveEmployeeException.class,
            NoLicenseSeatsAvailableException.class,
            LicenseAlreadyAssignedException.class,
            EmployeeHasActiveAssetsException.class,
            DuplicateSerialNumberException.class,
            DuplicateEmployeeException.class,
            DuplicateLicenseException.class,
            MaintenanceStateException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request, null, ex);
    }

    /**
     * Builds an error response for the supplied exception scenario.
     *
     * @param ex the captured exception
     * @param request the request payload
     * @return the resulting global exception handler
     */
    @ExceptionHandler(LicenseExpiredException.class)
    public ResponseEntity<ErrorResponse> handleLicenseExpired(LicenseExpiredException ex, HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null, ex);
    }

    /**
     * Builds an error response for the supplied exception scenario.
     *
     * @param ex the captured exception
     * @param request the request payload
     * @return the resulting global exception handler
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        return error(HttpStatus.BAD_REQUEST, "Request validation failed", request, fieldErrors, ex);
    }

    /**
     * Builds an error response for the supplied exception scenario.
     *
     * @param ex the captured exception
     * @param request the request payload
     * @return the resulting global exception handler
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                fieldErrors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        return error(HttpStatus.BAD_REQUEST, "Request parameter validation failed", request, fieldErrors, ex);
    }

    /**
     * Builds an error response for the supplied exception scenario.
     *
     * @param ex the captured exception
     * @param request the request payload
     * @return the resulting global exception handler
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        fieldErrors.put(ex.getParameterName(), "must be provided");
        return error(HttpStatus.BAD_REQUEST, "Request parameter validation failed", request, fieldErrors, ex);
    }

    /**
     * Builds an error response for the supplied exception scenario.
     *
     * @param ex the captured exception
     * @param request the request payload
     * @return the resulting global exception handler
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ErrorResponse> handleServletRequestBinding(
            ServletRequestBindingException ex,
            HttpServletRequest request
    ) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null, ex);
    }

    /**
     * Builds an error response for the supplied exception scenario.
     *
     * @param ex the captured exception
     * @param request the request payload
     * @return the resulting global exception handler
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedRequestBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return error(HttpStatus.BAD_REQUEST, "Malformed request body", request, null, ex);
    }

    /**
     * Builds an error response for the supplied exception scenario.
     *
     * @param ex the captured exception
     * @param request the request payload
     * @return the resulting global exception handler
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null, ex);
    }

    /**
     * Builds an error response for the supplied exception scenario.
     *
     * @param ex the captured exception
     * @param request the request payload
     * @return the resulting global exception handler
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, "Request conflicts with existing data", request, null, ex);
    }

    /**
     * Builds an error response for the supplied exception scenario.
     *
     * @param ex the captured exception
     * @param request the request payload
     * @return the resulting global exception handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request, null, ex);
    }

    /**
     * Executes the error operation.
     *
     * @param status the requested status value
     * @param message the exception detail message
     * @param request the request payload
     * @param fieldErrors the field errors value
     * @param ex the captured exception
     * @return the resulting global exception handler
     */
    private ResponseEntity<ErrorResponse> error(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors,
            Exception ex
    ) {
        if (status.is5xxServerError()) {
            log.error("{} at {}", message, request.getRequestURI(), ex);
        } else {
            log.error("{} at {}", message, request.getRequestURI());
        }
        return ResponseEntity.status(status).body(new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                reasonPhrase(status),
                message,
                request.getRequestURI(),
                fieldErrors
        ));
    }

    /**
     * Executes the reason phrase operation.
     *
     * @param status the requested status value
     * @return the resulting global exception handler
     */
    private String reasonPhrase(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Bad Request";
            case NOT_FOUND -> "Not Found";
            case CONFLICT -> "Conflict";
            case UNPROCESSABLE_CONTENT -> "Unprocessable Entity";
            case INTERNAL_SERVER_ERROR -> "Internal Server Error";
            /**
             * Executes the humanize operation.
             *
             * @param status.name() the status.name() value
             * @return the resulting global exception handler
             */
            default -> humanize(status.name());
        };
    }

    /**
     * Executes the humanize operation.
     *
     * @param value the value to inspect
     * @return the resulting global exception handler
     */
    private String humanize(String value) {
        StringBuilder result = new StringBuilder();
        for (String word : value.split("_")) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(word.charAt(0));
            result.append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return result.toString();
    }
}
