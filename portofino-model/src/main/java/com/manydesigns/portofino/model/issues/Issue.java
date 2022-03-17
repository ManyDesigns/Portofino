package com.manydesigns.portofino.model.issues;

import org.eclipse.emf.ecore.EObject;

public class Issue {

    public final Severity severity;
    public final EObject object;
    public final String message;
    public final String path;
    public final Integer line;
    public final Integer column;

    public Issue(Severity severity, EObject object, String message, String path, Integer line, Integer column) {
        this.severity = severity;
        this.object = object;
        this.message = message;
        this.path = path;
        this.line = line;
        this.column = column;
    }

    public enum Severity {
        INFO, WARNING, ERROR
    }

}
