package com.henrique.virtualteacher.exceptions;

public class DuplicateEntityException extends RuntimeException{

    public DuplicateEntityException(String type, String field, String fieldValue) {
        super(String.format("%s with %s: %s, already exists", type, field, fieldValue));
    }

    public DuplicateEntityException(String message) {
        super(message);
    }

}
