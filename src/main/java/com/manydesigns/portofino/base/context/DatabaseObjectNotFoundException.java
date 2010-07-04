package com.manydesigns.portofino.base.context;

public class DatabaseObjectNotFoundException extends Exception {
    public DatabaseObjectNotFoundException() {
    }

    public DatabaseObjectNotFoundException(String s) {
        super(s);
    }

    public DatabaseObjectNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DatabaseObjectNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
