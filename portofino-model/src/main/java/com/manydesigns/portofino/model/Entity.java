package com.manydesigns.portofino.model;

import org.apache.commons.configuration2.Configuration;

import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Entity implements ModelObject, Annotated {

    protected Domain domain;
    protected String name;
    protected final List<Property> properties = new ArrayList<>();
    protected final List<Property> id = new ArrayList<>();
    protected final List<Annotation> annotations = new ArrayList<>();
    protected final List<Relationship> relationships = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setParent(Object parent) {
        setDomain((Domain) parent);
    }

    @Override
    public void reset() {

    }

    @Override
    public void init(Model model, Configuration configuration) {
        if(!properties.containsAll(id)) {
            throw new RuntimeException("Not all id properties belong to this entity"); //TODO
        }
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

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        if(this.domain == domain) {
            return;
        }
        this.domain = domain;
        domain.addEntity(this);
    }

    public void addProperty(Property property) {
        Entity owner = property.owner;
        if(owner == null) {
            Property existing = findProperty(property.name);
            if(existing != null && !existing.equals(property)) {
                throw new IllegalArgumentException("A property named " + property.name + " already exists in entity " + this);
            }
            property.setOwner(this);
            properties.add(property);
        } else if(owner != this) {
            throw new IllegalArgumentException("Property " + property + " already belongs to entity " + owner);
        }
    }

    public Property findProperty(String name) {
        for(Property p : getProperties()) {
            if(p.name.equals(name)) {
                return p;
            }
        }
        return null;
    }

    public void addRelationship(Entity other) {
        Relationship r = new Relationship(this, other);
        domain.addRelationship(r);
    }

    public Collection<Relationship> getRelationships() {
        return relationships;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<Property> getId() {
        return id;
    }
}
