package com.manydesigns.portofino.model;

import org.apache.commons.configuration2.Configuration;

import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.List;

public class Domain implements ModelObject, Annotated {

    protected String name;

    protected final List<Domain> parents = new ArrayList<>();
    protected final List<Domain> subdomains = new ArrayList<>();
    protected final List<Annotation> annotations = new ArrayList<>();
    protected final List<Type> types = new ArrayList<>();
    protected final List<Entity> entities = new ArrayList<>();
    protected final List<Relationship> relationships = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void afterUnmarshal(Unmarshaller u, Object parent) {
        parents.add((Domain) parent);
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

    @Override
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void addEntity(Entity entity) {
        Domain domain = entity.getDomain();
        if(domain == null) {
            entity.setDomain(this);
        } else if(domain != this) {
            throw new IllegalArgumentException("Entity " + entity + " already belongs to domain " + domain);
        }
        entities.add(entity);
    }

    public Entity findEntity(String name) {
        for (Entity e : entities) {
            if(e.getName().equals(name)) {
                return e;
            }
        }
        for(Domain dom : parents) {
            Entity entity = dom.findEntity(name);
            if(entity != null) {
                return entity;
            }
        }
        return null;
    }

    public void addRelationship(Relationship r) {
        if(!r.getA().equals(findEntity(r.getA().getName()))) {
            throw new IllegalArgumentException("Entity " + r.getA() + " does not belong to " + this);
        }
        if(!r.getB().equals(findEntity(r.getB().getName()))) {
            throw new IllegalArgumentException("Entity " + r.getB() + " does not belong to " + this);
        }
        r.getA().getRelationships().add(r);
        r.getB().getRelationships().add(r);
    }

    public List<Domain> getParents() {
        return parents;
    }

    public List<Domain> getSubdomains() {
        return subdomains;
    }

    public List<Type> getTypes() {
        return types;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }
}
