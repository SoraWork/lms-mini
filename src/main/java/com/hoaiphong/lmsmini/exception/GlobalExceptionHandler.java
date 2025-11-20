package com.hoaiphong.lmsmini.exception;

import com.hoaiphong.lmsmini.configuration.Translator;  // Import Translator để sử dụng i18n
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Helper method để tạo ErrorResponse chuẩn (DRY, tích hợp i18n)
    private ErrorResponse createErrorResponse(WebRequest request, int status, String code, String errorKey, String message) {
        ErrorResponse response = new ErrorResponse();
        response.setTimestamp(new Date());
        response.setStatus(status);
        response.setPath(request.getDescription(false).replace("uri=", ""));
        response.setCode(code);
        response.setError(Translator.toLocale(errorKey));  // Localize error field
        response.setMessage(message);
        return response;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleValidationErrors(Exception ex, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (ex instanceof MethodArgumentNotValidException validEx) {
            // Xử lý field errors từ @Valid DTO, localize defaultMessage (giả sử là key)
            validEx.getBindingResult().getFieldErrors().forEach(err ->
                    errors.put(err.getField(), Translator.toLocale(err.getDefaultMessage()))
            );
        } else if (ex instanceof ConstraintViolationException constraintEx) {
            // Xử lý constraint violations, localize message
            constraintEx.getConstraintViolations().forEach(cv ->
                    errors.put(cv.getPropertyPath().toString(), Translator.toLocale(cv.getMessage()))
            );
        } else {
            errors.put("unknown", Translator.toLocale("error.validation.unknown"));
        }

        // Concatenate errors thành message
        String message = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));

        String errorKey = ex instanceof MethodArgumentNotValidException ? "error.validation.body" :
                ex instanceof ConstraintViolationException ? "error.validation.param" : "error.validation.unknown";

        ErrorResponse errorResponse = createErrorResponse(request, HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", errorKey, message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String paramName = ex.getName();
        String inputValue = ex.getValue() != null ? ex.getValue().toString() : "unknown";
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String detailMessage = String.format("Failed to convert '%s' to %s: invalid input '%s'", paramName, requiredType, inputValue);
        String message = Translator.toLocale("error.type.mismatch") + ": " + detailMessage;  // Kết hợp i18n + chi tiết

        ErrorResponse errorResponse = createErrorResponse(request, HttpStatus.BAD_REQUEST.value(), "TYPE_MISMATCH", "error.type.mismatch", message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(SomeThingWrongException.class)
    public ResponseEntity<ErrorResponse> handleSomeThingWrongException(SomeThingWrongException ex, WebRequest request) {
        // ex.getMessage() là key, resolve thành localized message
        String localizedMessage = Translator.toLocale(ex.getMessageKey());  // Sử dụng messageKey từ exception

        ErrorResponse errorResponse = createErrorResponse(request, HttpStatus.BAD_REQUEST.value(), "SOMETHING_WRONG", "error.something.wrong", localizedMessage);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // Fallback handler cho tất cả exception khác (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        // Log full stack trace nếu cần (ex.printStackTrace() hoặc dùng logger)
        String localizedMessage = Translator.toLocale("error.internal.server");

        ErrorResponse errorResponse = createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", "error.internal.server", localizedMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}