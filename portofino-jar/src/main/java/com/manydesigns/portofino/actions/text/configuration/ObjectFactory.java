package com.manydesigns.portofino.actions.text.configuration;

import com.manydesigns.portofino.actions.jsp.configuration.JspConfiguration;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    public TextConfiguration createTextConfiguration() { return new TextConfiguration(); }
}
