package com.manydesigns.portofino.persistence;

public class PersistenceNotStartedException extends IllegalStateException {

    protected Persistence.Status status;

    public PersistenceNotStartedException(Throwable cause) {
        super("Persistence not started", cause);
    }

    public PersistenceNotStartedException(Throwable cause, Persistence.Status status) {
        super("Persistence not started, current status is " + status, cause);
        this.status = status;
    }

    public PersistenceNotStartedException(Persistence.Status status) {
        super("Persistence not started, current status is " + status);
        this.status = status;
    }

    public PersistenceNotStartedException() {
        super("Persistence not started");
    }

    public Persistence.Status getStatus() {
        return status;
    }
}
