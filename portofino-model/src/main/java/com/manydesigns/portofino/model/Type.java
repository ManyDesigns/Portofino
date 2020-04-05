package com.manydesigns.portofino.model;

import org.apache.commons.configuration2.Configuration;

import java.util.ArrayList;
import java.util.List;

public class Type implements ModelObject, Annotated {

    protected String name;

    protected final List<Annotation> annotations = new ArrayList<>();

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setParent(Object parent) {}

    @Override
    public void reset() {

    }

    @Override
    public void init(Model model, Configuration configuration) {

    }

    @Override
    public void link(Model model, Configuration configuration) {

    }

    @Override
    public void visitChildren(ModelObjectVisitor visitor) {
        for (Annotation annotation : annotations) {
            visitor.visit(annotation);
        }
    }

    @Override
    public List<Annotation> getAnnotations() {
        return annotations;
    }

}
