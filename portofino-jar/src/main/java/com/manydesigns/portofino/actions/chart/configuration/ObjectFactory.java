package com.manydesigns.portofino.actions.chart.configuration;

import com.manydesigns.portofino.actions.jsp.configuration.JspConfiguration;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    public ChartConfiguration createChartConfiguration() { return new ChartConfiguration(); }
}
