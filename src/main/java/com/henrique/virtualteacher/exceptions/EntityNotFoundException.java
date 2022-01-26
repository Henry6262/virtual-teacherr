package com.henrique.virtualteacher.exceptions;

public class EntityNotFoundException extends RuntimeException{

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String type, String field, String fieldValue) {
        super(String.format("%s with %s: %s does not Exist", type, field, fieldValue));
    }
}
