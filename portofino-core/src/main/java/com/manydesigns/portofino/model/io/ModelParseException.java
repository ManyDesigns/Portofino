package com.manydesigns.portofino.model.io;

public class ModelParseException extends RuntimeException {

    public ModelParseException() {}

    public ModelParseException(String message) {
        super(message);
    }

    public ModelParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelParseException(Throwable cause) {
        super(cause);
    }

    public ModelParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
