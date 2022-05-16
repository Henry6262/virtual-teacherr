package com.henrique.virtualteacher.models;



public enum TransactionType {

    TRANSFER, EXCHANGE, DEPOSIT, PURCHASE;

    public String toString() {
        return name().substring(0,1).toUpperCase().concat(name().substring(1));
    }

}
