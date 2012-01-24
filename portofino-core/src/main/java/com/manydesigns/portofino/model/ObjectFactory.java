package com.manydesigns.portofino.model;

import com.manydesigns.portofino.model.Model;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    public Model createModel() { return new Model(); }
}
