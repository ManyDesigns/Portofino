package com.manydesigns.portofino.model;

import org.apache.commons.configuration2.Configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class AnnotationProperty implements ModelObject {

    protected Annotation owner;
    protected String name;
    protected String value;

    public AnnotationProperty() {}

    public AnnotationProperty(Annotation annotation, String name, String value) {
        setName(name);
        setValue(value);
        setParent(annotation);
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
        return owner != null ? owner.eAnnotation.getDetails().get(name) : value;
    }

    public void setValue(String value) {
        if(owner != null) {
            owner.eAnnotation.getDetails().put(name, value);
        } else {
            this.value = value;
        }
    }

    @Override
    public void setParent(Object parent) {
        String value = getValue();
        this.owner = (Annotation) parent;
        setValue(value);
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
