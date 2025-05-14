package com.store.grocery.util.exception;

import com.store.grocery.dto.response.RestResponse;
import com.store.grocery.util.constants.StatusCodeConstant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<RestResponse<Object>> handleCredentialException(RuntimeException ex) {
        return buildResponse(StatusCodeConstant.CREDENTIAL_EXCEPTION_STATUS, "Authentication error", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> handleValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<String> errors = result.getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        return buildResponse(StatusCodeConstant.VALIDATION_EXCEPTION_STATUS, "Validation Error",
                errors.size() > 1 ? errors : errors.get(0), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<RestResponse<Object>> handleFileUploadException(StorageException ex) {
        return buildResponse(StatusCodeConstant.STORAGE_EXCEPTION_STATUS, "Upload file exception!", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<RestResponse<Object>> handleAuthException(AuthException ex) {
        return buildResponse(StatusCodeConstant.AUTH_EXCEPTION_STATUS, "Authentication failed!", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceInvalidException.class)
    public ResponseEntity<RestResponse<Object>> handleResourceException(ResourceInvalidException ex) {
        return buildResponse(StatusCodeConstant.RESOURCE_INVALID_EXCEPTION_STATUS, "Resource not found", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleUserNotFoundException(UserNotFoundException ex) {
        return buildResponse(StatusCodeConstant.USER_NOT_FOUND_EXCEPTION_STATUS, "User not found in system", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<RestResponse<Object>> handleDuplicateResourceException(DuplicateResourceException ex) {
        return buildResponse(StatusCodeConstant.DUPLICATE_RESOURCE_EXCEPTION_STATUS, "Duplicate resource", ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(StatusCodeConstant.RESOURCE_NOT_FOUND_EXCEPTION_STATUS, "Resource not found in system", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CannotDeleteException.class)
    public ResponseEntity<RestResponse<Object>> handleCannotDeleteException(CannotDeleteException ex) {
        return buildResponse(StatusCodeConstant.CANNOT_DELETE_EXCEPTION_STATUS, "Resources that should not be deleted", ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleNotFoundException(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND.value(), "404 Not Found. URL may not exist...",
                ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleAllException(Exception ex) {
        return buildResponse(StatusCodeConstant.EXCEPTION_STATUS, "Internal Server Error", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<RestResponse<Object>> buildResponse(int statusCode, String error, Object message, HttpStatus status) {
        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(statusCode);
        response.setError(error);
        response.setMessage(message);
        return ResponseEntity.status(status).body(response);
    }
}
