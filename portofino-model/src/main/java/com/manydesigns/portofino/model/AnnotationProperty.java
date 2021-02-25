package com.manydesigns.portofino.model;

import org.apache.commons.configuration2.Configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class AnnotationProperty implements ModelObject {

    protected Annotation owner;
    protected String name;

    public AnnotationProperty() {}

    public AnnotationProperty(Annotation annotation, String name, String value) {
        setParent(annotation);
        this.name = name;
        setValue(value);
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute
    public String getValue() {
        return owner.eAnnotation.getDetails().get(name);
    }

    public void setValue(String value) {
        owner.eAnnotation.getDetails().put(name, value);
    }

    @Override
    public void setParent(Object parent) {
        this.owner = (Annotation) parent;
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

    }
}
