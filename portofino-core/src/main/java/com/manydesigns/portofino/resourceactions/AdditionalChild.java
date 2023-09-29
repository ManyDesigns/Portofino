package com.manydesigns.portofino.resourceactions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * Represents a child action from another path.
 * @author Alessio Stalla – alessiostalla@gmail.com
 */
@XmlAccessorType(XmlAccessType.NONE)
public class AdditionalChild {

    protected String segment;
    protected String path;

    @XmlAttribute
    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    @XmlAttribute
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
