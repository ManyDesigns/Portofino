package com.manydesigns.portofino.model;

import org.apache.commons.configuration2.Configuration;

import java.util.ArrayList;
import java.util.List;

public class Property implements ModelObject, Annotated {

    protected Entity owner;
    protected String name;
    protected Type type;
    protected final List<Annotation> annotations = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void setParent(Object parent) {
        this.owner = (Entity) parent;
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
        for (Annotation annotation : annotations) {
            visitor.visit(annotation);
        }
    }

    public Entity getOwner() {
        return owner;
    }

    public void setOwner(Entity owner) {
        if(this.owner == owner) {
            return;
        } else if(this.owner != null) {
            throw new IllegalArgumentException("Cannot change owner");
        }
        this.owner = owner;
        owner.addProperty(this);
    }

    @Override
    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
