package com.manydesigns.portofino.pageactions.crud.configuration;

import com.manydesigns.elements.annotations.CssClass;
import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.util.BootstrapSizes;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.ConfigurationWithDefaults;
import com.manydesigns.portofino.dispatcher.PageActionConfiguration;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.Persistence;

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
@XmlType(name = "configuration",propOrder = {"name", "searchTitle","createTitle","readTitle","editTitle","variable","largeResultSet","rowsPerPage","columns","useLocalOrder","properties"})
@XmlAccessorType(value = XmlAccessType.NONE)
public class CrudConfiguration implements PageActionConfiguration, ConfigurationWithDefaults {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

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
    protected Integer columns = 1;

    @Inject(DatabaseModule.PERSISTENCE)
    public Persistence persistence;

    public CrudConfiguration() {
        properties = new ArrayList<CrudProperty>();
    }

    public void init() {
        for (CrudProperty property : properties) {
            property.init(persistence.getModel(), persistence.getConfiguration());
        }
    }

    public void setupDefaults() {
        rowsPerPage = 10;
    }

    @XmlElementWrapper(name="properties")
    @XmlElements({
        @XmlElement(name="property",type=CrudProperty.class),
        @XmlElement(name="virtual-property",type=VirtualCrudProperty.class),
    })
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

    @CssClass(BootstrapSizes.FILL_ROW)
    @XmlAttribute(required = false)
    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }

    @CssClass(BootstrapSizes.FILL_ROW)
    @XmlAttribute(required = false)
    public String getCreateTitle() {
        return createTitle;
    }

    public void setCreateTitle(String createTitle) {
        this.createTitle = createTitle;
    }

    @CssClass(BootstrapSizes.FILL_ROW)
    @XmlAttribute(required = false)
    public String getReadTitle() {
        return readTitle;
    }

    public void setReadTitle(String readTitle) {
        this.readTitle = readTitle;
    }

    @CssClass(BootstrapSizes.FILL_ROW)
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

    @CssClass(BootstrapSizes.COL_SM_1)
    @XmlAttribute(required = false)
    public Integer getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(Integer rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    @Required
    @CssClass(BootstrapSizes.COL_SM_1)
    @XmlAttribute(required = false)
    public Integer getColumns() {
        return columns;
    }

    public void setColumns(Integer columns) {
        this.columns = columns;
    }

    @XmlAttribute(required = false)
    public boolean isUseLocalOrder() {
        return useLocalOrder;
    }

    public void setUseLocalOrder(boolean useLocalOrder) {
        this.useLocalOrder = useLocalOrder;
    }
}
