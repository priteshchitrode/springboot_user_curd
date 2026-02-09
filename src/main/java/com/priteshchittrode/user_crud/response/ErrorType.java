package com.priteshchittrode.user_crud.response;
import org.springframework.http.HttpStatus;

public abstract class ErrorType {
    private final HttpStatus httpStatus;
    private final String message;

    public ErrorType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + message + "}";
    }


    // Input Validation Errors
    public static class ValidationError extends ErrorType {
        public ValidationError(String fieldName, String message) {
            super(HttpStatus.BAD_REQUEST, fieldName + ": " + message);
        }
    }

    public static class InvalidEmailError extends ErrorType {
        public InvalidEmailError() {
            super(HttpStatus.BAD_REQUEST, "Invalid email format");
        }
    }

    public static class InvalidPasswordError extends ErrorType {
        public InvalidPasswordError(String message) {
            super(HttpStatus.BAD_REQUEST, message);
        }
    }

    public static class FieldRequiredError extends ErrorType {
        public FieldRequiredError(String fieldName) {
            super(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
    }


    // Authentication Errors
    public static class InvalidCredentialsError extends ErrorType {
        public InvalidCredentialsError() {
            super(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }

    public static class UnauthenticatedError extends ErrorType {
        public UnauthenticatedError() {
            super(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
    }

    public static class TokenExpiredError extends ErrorType {
        public TokenExpiredError() {
            super(HttpStatus.UNAUTHORIZED, "Token has expired");
        }
    }

    public static class InvalidTokenError extends ErrorType {
        public InvalidTokenError() {
            super(HttpStatus.UNAUTHORIZED, "Invalid or malformed token");
        }
    }


    public static class RefreshTokenMismatchError extends ErrorType {
        public RefreshTokenMismatchError() {
            super(HttpStatus.UNAUTHORIZED, "Refresh token does not match");
        }
    }


    // Resource Errors
    public static class ConflictError extends ErrorType {
        public ConflictError(String message) {
            super(HttpStatus.CONFLICT, message);
        }
    }


    public static class NotFoundError extends ErrorType {
        public NotFoundError(String resource) {
            super(HttpStatus.NOT_FOUND,  resource);
        }
    }

    public static class ResourceNotFoundError extends ErrorType {
        public ResourceNotFoundError(String resource) {
            super(HttpStatus.NOT_FOUND, resource + " not found");
        }
    }

    public static class DuplicateEmailError extends ErrorType {
        public DuplicateEmailError() {
            super(HttpStatus.CONFLICT, "Email already exists");
        }
    }


    // Authorization Errors
    public static class ForbiddenError extends ErrorType {
        public ForbiddenError(String message) {
            super(HttpStatus.FORBIDDEN, message);
        }
    }

    public static class AccessDeniedError extends ErrorType {
        public AccessDeniedError() {
            super(HttpStatus.FORBIDDEN, "Access denied");
        }
    }


    // Server Errors
    public static class InternalServerError extends ErrorType {
        public InternalServerError(String message) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }
    }

    public static class DeserializationError extends ErrorType {
        public DeserializationError(String message) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process data: " + message);
        }
    }


    //  Request Errors
    public static class BadRequestError extends ErrorType {
        public BadRequestError(String message) {
            super(HttpStatus.BAD_REQUEST, message);
        }
    }

    public static class MissingHeaderError extends ErrorType {
        public MissingHeaderError(String headerName) {
            super(HttpStatus.BAD_REQUEST, "Missing required header: " + headerName);
        }
    }

}