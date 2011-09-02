package com.manydesigns.portofino.context;

public class ModelObjectNotFoundError extends Error {
    public ModelObjectNotFoundError() {
    }

    public ModelObjectNotFoundError(String s) {
        super(s);
    }

    public ModelObjectNotFoundError(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ModelObjectNotFoundError(Throwable throwable) {
        super(throwable);
    }
}
