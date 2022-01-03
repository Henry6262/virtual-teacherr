package com.henrique.virtualteacher.models;

public class MessageBuilder {

    public static  String createSizeMsg(String fieldName, int minLength, int maxLength) {
        return String.format("%s, must be between %d and %d characters", fieldName, minLength, maxLength);
    }

}
