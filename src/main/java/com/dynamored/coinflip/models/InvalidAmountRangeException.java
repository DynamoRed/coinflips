package com.dynamored.coinflip.models;

public class InvalidAmountRangeException extends Exception {

    public InvalidAmountRangeException() {
        super();
    }

    public InvalidAmountRangeException(String message) {
        super(message);
    }

    public InvalidAmountRangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAmountRangeException(Throwable cause) {
        super(cause);
    }
}
