package com.manydesigns.portofino.model;

import org.apache.commons.configuration2.Configuration;

import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.List;

public class Entity implements ModelObject, Annotated {

    protected String name;
    protected final List<Property> properties = new ArrayList<>();
    protected final List<Annotation> annotations = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void afterUnmarshal(Unmarshaller u, Object parent) {

    }

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
        for(Property property : properties) {
            visitor.visit(property);
        }
        for (Annotation annotation : annotations) {
            visitor.visit(annotation);
        }
    }

    @Override
    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
