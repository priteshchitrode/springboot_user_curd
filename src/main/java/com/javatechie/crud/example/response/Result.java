package com.javatechie.crud.example.response;

public abstract class Result<T> {
    private Result() {}

    public static final class Success<T> extends Result<T> {
        public final T value;

        public Success(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Success(" + value + ")";
        }
    }


    public static final class Error<T> extends Result<T> {
        public final ErrorType errorType;

        public Error(ErrorType errorType) {
            this.errorType = errorType;
        }

        @Override
        public String toString() {
            return "Error(" + errorType + ")";
        }
    }


    // Helper methods
    public boolean isSuccess() {
        return this instanceof Success;
    }

    public boolean isError() {
        return this instanceof Error;
    }

    public T getValueOrNull() {
        return this instanceof Success ? ((Success<T>) this).value : null;
    }

    public ErrorType getErrorOrNull() {
        return this instanceof Error ? ((Error<T>) this).errorType : null;
    }

}