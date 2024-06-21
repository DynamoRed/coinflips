package com.dynamored.coinflip.models;

public class IllegalGameEnd extends Exception {

    public IllegalGameEnd() {
        super();
    }

    public IllegalGameEnd(String message) {
        super(message);
    }

    public IllegalGameEnd(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalGameEnd(Throwable cause) {
        super(cause);
    }
}
