package com.manydesigns.portofino.context;

public class ModelObjectNotFoundException extends Exception {
    public ModelObjectNotFoundException() {
    }

    public ModelObjectNotFoundException(String s) {
        super(s);
    }

    public ModelObjectNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ModelObjectNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
