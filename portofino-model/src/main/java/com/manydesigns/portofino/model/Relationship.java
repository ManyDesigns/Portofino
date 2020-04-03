package com.manydesigns.portofino.model;

import org.apache.commons.configuration2.Configuration;

import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.List;

public class Relationship implements ModelObject, Annotated, Named {

    public Relationship() {}

    public Relationship(Entity a, Entity b) {
        this.a = a;
        this.b = b;
    }

    protected String name;
    protected Entity a;
    protected Entity b;

    protected final List<Annotation> annotations = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void afterUnmarshal(Unmarshaller u, Object parent) {}

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

    public Entity getA() {
        return a;
    }

    public void setA(Entity a) {
        this.a = a;
    }

    public Entity getB() {
        return b;
    }

    public void setB(Entity b) {
        this.b = b;
    }

}
