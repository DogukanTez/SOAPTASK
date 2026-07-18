package com.dogukantez.soaptask.exception;


public abstract class ViesException extends RuntimeException {

    protected ViesException(String message, Throwable cause) {
        super(message, cause);
    }
}
