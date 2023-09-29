/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.resourceactions.crud.configuration.database;

import com.manydesigns.elements.options.DisplayMode;
import com.manydesigns.elements.options.SearchDisplayMode;
import com.manydesigns.portofino.database.model.DatabaseLogic;
import com.manydesigns.portofino.database.model.ForeignKey;
import com.manydesigns.portofino.database.model.ModelSelectionProvider;
import com.manydesigns.portofino.database.model.Table;
import org.apache.commons.lang.StringUtils;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"enabled", "selectionProviderName","foreignKeyName","displayModeName","searchDisplayModeName","createNewValueHref","createNewValueText"})
public class SelectionProviderReference {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String foreignKeyName;
    protected boolean enabled = true;
    protected String displayModeName;
    protected String searchDisplayModeName;
    protected String selectionProviderName;

    protected String createNewValueHref;
    protected String createNewValueText;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected ForeignKey foreignKey;
    protected DisplayMode displayMode;
    protected SearchDisplayMode searchDisplayMode;
    protected ModelSelectionProvider selectionProvider;

    public void init(Table table) {
        if(displayModeName != null) {
            displayMode = DisplayMode.valueOf(displayModeName);
        } else {
            displayMode = DisplayMode.DROPDOWN;
        }
        if(searchDisplayModeName != null) {
            searchDisplayMode = SearchDisplayMode.valueOf(searchDisplayModeName);
        } else {
            searchDisplayMode = SearchDisplayMode.DROPDOWN;
        }

        if(!StringUtils.isEmpty(foreignKeyName)) {
            foreignKey = DatabaseLogic.findForeignKeyByName(table, foreignKeyName);
        } else if(!StringUtils.isEmpty(selectionProviderName)) {
            selectionProvider = DatabaseLogic.findSelectionProviderByName(table, selectionProviderName);
        } else {
            throw new Error("foreignKey and selectionProvider are both null");
        }
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlAttribute(name = "fk")
    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public void setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
    }

    @XmlAttribute(name = "selectionProvider")
    public String getSelectionProviderName() {
        return selectionProviderName;
    }

    public void setSelectionProviderName(String selectionProviderName) {
        this.selectionProviderName = selectionProviderName;
    }

    public ForeignKey getForeignKey() {
        return foreignKey;
    }

    @XmlAttribute
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @XmlAttribute(name = "displayMode")
    public String getDisplayModeName() {
        return displayModeName;
    }

    public void setDisplayModeName(String displayModeName) {
        this.displayModeName = displayModeName;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
        displayModeName = displayMode.name();
    }

    @XmlAttribute(name = "searchDisplayMode")
    public String getSearchDisplayModeName() {
        return searchDisplayModeName;
    }

    public void setSearchDisplayModeName(String displayModeName) {
        this.searchDisplayModeName = displayModeName;
    }

    public SearchDisplayMode getSearchDisplayMode() {
        return searchDisplayMode;
    }

    public void setSearchDisplayMode(SearchDisplayMode displayMode) {
        this.searchDisplayMode = displayMode;
        searchDisplayModeName = displayMode.name();
    }

    public ModelSelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    public ModelSelectionProvider getActualSelectionProvider() {
        return foreignKey != null ? foreignKey : selectionProvider;
    }

    @XmlAttribute(name = "createNewValueHref")
    public String getCreateNewValueHref() {
        return createNewValueHref;
    }

    public void setCreateNewValueHref(String createNewValueHref) {
        this.createNewValueHref = createNewValueHref;
    }

    @XmlAttribute(name = "createNewValueText")
    public String getCreateNewValueText() {
        return createNewValueText;
    }

    public void setCreateNewValueText(String createNewValueText) {
        this.createNewValueText = createNewValueText;
    }
}
