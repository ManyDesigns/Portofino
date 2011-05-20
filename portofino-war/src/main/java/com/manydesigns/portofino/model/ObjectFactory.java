package com.manydesigns.portofino.model;

import com.manydesigns.portofino.connections.Connections;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    public Model createModel() { return new Model(); }
}
