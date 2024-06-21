package com.dynamored.coinflip.models;

public class IllegalGameCancellation extends Exception {

    public IllegalGameCancellation() {
        super();
    }

    public IllegalGameCancellation(String message) {
        super(message);
    }

    public IllegalGameCancellation(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalGameCancellation(Throwable cause) {
        super(cause);
    }
}
