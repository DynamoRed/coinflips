package com.dynamored.coinflip.models;

public class IllegalGameStart extends Exception {

    public IllegalGameStart() {
        super();
    }

    public IllegalGameStart(String message) {
        super(message);
    }

    public IllegalGameStart(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalGameStart(Throwable cause) {
        super(cause);
    }
}
