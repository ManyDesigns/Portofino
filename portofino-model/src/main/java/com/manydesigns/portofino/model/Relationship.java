package com.manydesigns.portofino.model;

import org.apache.commons.configuration2.Configuration;

import java.util.ArrayList;
import java.util.List;

public class Relationship implements ModelObject, Annotated, Named {

    protected Pair<Entity> entities;
    protected String name;

    public Relationship() {
        entities = new Pair<>();
    }

    public Relationship(Entity a, Entity b) {
        entities = new Pair<>(a, b);
    }

    protected final List<Annotation> annotations = new ArrayList<>();

    @Override
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

    public Entity getA() {
        return entities.left;
    }

    public void setA(Entity a) {
        entities.left = a;
    }

    public Entity getB() {
        return entities.right;
    }

    public void setB(Entity b) {
        entities.right = b;
    }

}
