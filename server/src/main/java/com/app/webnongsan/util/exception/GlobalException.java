package com.app.webnongsan.util.exception;

import com.app.webnongsan.domain.response.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {
    private static final Logger log = LoggerFactory.getLogger(GlobalException.class);

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<RestResponse<Object>> handleCredentialException(RuntimeException ex) {
        return buildResponse(-2, "Authentication error", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> handleValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<String> errors = result.getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        return buildResponse(HttpStatus.BAD_REQUEST.value(), "Validation Error",
                errors.size() > 1 ? errors : errors.get(0), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<RestResponse<Object>> handleFileUploadException(StorageException ex) {
        return buildResponse(-3, "Upload file exception!", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<RestResponse<Object>> handleAuthException(AuthException ex) {
        return buildResponse(-4, "Authentication failed!", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceInvalidException.class)
    public ResponseEntity<RestResponse<Object>> handleResourceException(ResourceInvalidException ex) {
        return buildResponse(-5, "Resource not found", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleUserNotFoundException(UserNotFoundException ex) {
        return buildResponse(-6, "User not found in system", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<RestResponse<Object>> handleDuplicateResourceException(DuplicateResourceException ex) {
        return buildResponse(-7, "Duplicate resource", ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CannotDeleteException.class)
    public ResponseEntity<RestResponse<Object>> handleCannotDeleteException(CannotDeleteException ex) {
        return buildResponse(-8, "Resources that should not be deleted", ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleNotFoundException(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND.value(), "404 Not Found. URL may not exist...",
                ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleAllException(Exception ex) {
        return buildResponse(-1, "Internal Server Error", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<RestResponse<Object>> buildResponse(int statusCode, String error, Object message, HttpStatus status) {
        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(statusCode);
        response.setError(error);
        response.setMessage(message);
        return ResponseEntity.status(status).body(response);
    }
}
