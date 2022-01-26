package com.henrique.virtualteacher.exceptions;

public class UnauthorizedOperationException extends RuntimeException{

    public UnauthorizedOperationException(String message) {
        super(message);
    }

    public UnauthorizedOperationException(String type, String field, String fieldValue,String operation ,String typeToGet,String fieldToGet, String valueToGet ) {
    super(String.format("%s, with %s: %s is not authorized to %s %s, with %s %s",
            type, field, fieldValue, operation, typeToGet, fieldToGet,  valueToGet));
    }
    public UnauthorizedOperationException(String type, String field, int fieldValue, String operation, String typeToGet, String valueToGet ) {
        super(String.format("%s, with %s: %s is not authorized to %s %s, with %s",
                type, field, fieldValue, operation, typeToGet, valueToGet));
    }

    public UnauthorizedOperationException(String type, String field, int fieldValue, String typeToGet, int valueToGet ) {
        super(String.format("%s, with %s: %s is not authorized to get %s, with %s",
                type, field, fieldValue, typeToGet, valueToGet));
    }

}
