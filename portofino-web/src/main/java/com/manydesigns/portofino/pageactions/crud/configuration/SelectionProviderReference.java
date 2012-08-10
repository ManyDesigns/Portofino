/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.pageactions.crud.configuration;

import com.manydesigns.elements.options.DisplayMode;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.ForeignKey;
import com.manydesigns.portofino.model.database.ModelSelectionProvider;
import com.manydesigns.portofino.model.database.Table;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@XmlAccessorType(XmlAccessType.NONE)
public class SelectionProviderReference {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String foreignKeyName;
    protected boolean enabled = true;
    protected String displayModeName;
    protected String selectionProviderName;

    protected String createNewValueHref;
    protected String createNewValueText;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected ForeignKey foreignKey;
    protected DisplayMode displayMode;
    protected ModelSelectionProvider selectionProvider;

    public void init(Table table) {
        if(displayModeName != null) {
            displayMode = DisplayMode.valueOf(displayModeName);
        } else {
            displayMode = DisplayMode.DROPDOWN;
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
