package com.manydesigns.portofino.resourceactions.crud.configuration;

import com.manydesigns.elements.annotations.Enabled;
import com.manydesigns.elements.annotations.Label;
import com.manydesigns.portofino.resourceactions.ConfigurationWithDefaults;
import com.manydesigns.portofino.resourceactions.ResourceActionConfiguration;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/

@XmlRootElement(name = "configuration")
@XmlType(name = "configuration",propOrder = {"name", "searchTitle","createTitle","readTitle","editTitle","variable","largeResultSet","rowsPerPage","useLocalOrder","properties"})
@XmlAccessorType(value = XmlAccessType.NONE)
public class CrudConfiguration implements ResourceActionConfiguration, ConfigurationWithDefaults {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<CrudProperty> properties;
    protected String name;
    protected String searchTitle;
    protected String createTitle;
    protected String readTitle;
    protected String editTitle;
    protected String variable;
    protected boolean largeResultSet;
    protected boolean useLocalOrder = false;
    protected Integer rowsPerPage;

    public CrudConfiguration() {
        properties = new ArrayList<>();
    }

    public void init() {}

    public void setupDefaults() {
        rowsPerPage = 10;
    }

    @XmlElementWrapper(name="properties")
    @XmlElements({
        @XmlElement(name="property",type=CrudProperty.class),
        @XmlElement(name="virtual-property",type=VirtualCrudProperty.class),
    })
    @Enabled(false)
    public List<CrudProperty> getProperties() {
        return properties;
    }

    @Label("name")
    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(required = false)
    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }

    @XmlAttribute(required = false)
    public String getCreateTitle() {
        return createTitle;
    }

    public void setCreateTitle(String createTitle) {
        this.createTitle = createTitle;
    }

    @XmlAttribute(required = false)
    public String getReadTitle() {
        return readTitle;
    }

    public void setReadTitle(String readTitle) {
        this.readTitle = readTitle;
    }

    @XmlAttribute(required = false)
    public String getEditTitle() {
        return editTitle;
    }

    public void setEditTitle(String editTitle) {
        this.editTitle = editTitle;
    }

    @XmlAttribute(required = false)
    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    @Enabled(false)
    public String getActualVariable() {
        return variable != null ? variable : name;
    }

    @XmlAttribute(required = true)
    public boolean isLargeResultSet() {
        return largeResultSet;
    }

    public void setLargeResultSet(boolean largeResultSet) {
        this.largeResultSet = largeResultSet;
    }

    @XmlAttribute(required = false)
    public Integer getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(Integer rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    @XmlAttribute(required = false)
    public boolean isUseLocalOrder() {
        return useLocalOrder;
    }

    public void setUseLocalOrder(boolean useLocalOrder) {
        this.useLocalOrder = useLocalOrder;
    }
}
