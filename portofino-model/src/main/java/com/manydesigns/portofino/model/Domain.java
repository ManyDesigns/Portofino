package com.manydesigns.portofino.model;

import org.apache.commons.configuration2.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Domain implements ModelObject, Annotated {

    protected String name;
    protected Domain parent;

    protected final List<Domain> imports = new ArrayList<>();
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
    public void setParent(Object parent) {
        if(this.parent == null) {
            Domain parentDomain = (Domain) parent;
            this.parent = parentDomain;
            parentDomain.getSubdomains().add(this);
        } else if(!this.parent.equals(parent)) {
            throw new IllegalStateException("Domain " + this + " already has a different parent");
        }
    }

    @Override
    public void reset() {
        for(Annotation a : annotations) {
            a.reset();
        }
    }

    @Override
    public void init(Model model, Configuration configuration) {
        for(Annotation a : annotations) {
            a.init(model, configuration);
        }
    }

    @Override
    public void link(Model model, Configuration configuration) {
        for(Annotation a : annotations) {
            a.link(model, configuration);
        }
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

    public Domain ensureSubdomain(String name) {
        return getSubdomains().stream().filter(d -> d.getName().equals(name)).findFirst().orElseGet(() -> {
            Domain domain = new Domain();
            domain.setName(name);
            domain.setParent(this);
            domain.getImports().addAll(imports); //TODO think better about the design
            return domain;
        });
    }

    public void addEntity(Entity entity) {
        Entity existing = findEntity(entity.name);
        if(existing != null && !existing.equals(entity)) {
            throw new IllegalArgumentException("An entity named " + entity.name + " already exists in domain " + this);
        }
        entity.setDomain(this);
        if(!entities.contains(entity)) {
            entities.add(entity);
        }
    }

    public Entity findEntity(String name) {
        if(name == null) {
            return null;
        }
        for (Entity e : entities) {
            if(name.equals(e.name)) {
                return e;
            }
        }
        for(Domain dom : imports) {
            Entity entity = dom.findEntity(name);
            if(entity != null) {
                return entity;
            }
        }
        return null;
    }

    public Entity ensureEntity(String name) {
        Entity entity = findEntity(name);
        if(entity == null) {
            entity = new Entity();
            entity.setName(name);
            addEntity(entity);
        }
        return entity;
    }

    public Type findType(String name) {
        for (Type t : types) {
            if(name.equals(t.getAlias()) || name.equals(t.getName())) {
                return t;
            }
        }
        for(Domain dom : imports) {
            Type type = dom.findType(name);
            if(type != null) {
                return type;
            }
        }
        if(parent != null) {
            return parent.findType(name);
        }
        return null;
    }

    public Type ensureType(String name) {
        Type type = findType(name);
        if(type == null) {
            type = new Type(name);
            type.setOwner(this);
            types.add(type);
        }
        return type;
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

    public Domain getParent() {
        return parent;
    }

    public List<Domain> getImports() {
        return imports;
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

    public Type getDefaultType() {
        for(Domain imported : imports) {
            Type defaultType = imported.getDefaultType();
            if(defaultType != null) {
                return defaultType;
            }
        }
        if(parent != null) {
            return parent.getDefaultType();
        }
        return null;
    }
}
