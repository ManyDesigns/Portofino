/*
* Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.model.pages.crud;

import com.manydesigns.elements.options.DisplayMode;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelVisitor;
import com.manydesigns.portofino.model.datamodel.ForeignKey;
import com.manydesigns.portofino.model.datamodel.ModelSelectionProvider;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.Unmarshaller;
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
public class SelectionProviderReference implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Crud parent;
    protected String foreignKeyName;
    protected boolean enabled = true;
    protected String displayModeName;
    protected String selectionProviderName;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected ForeignKey foreignKey;
    private DisplayMode displayMode;
    private ModelSelectionProvider selectionProvider;

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        this.parent = (Crud) parent;
    }

    public void reset() {
        foreignKey = null;
    }

    public void init() {
        if(displayModeName != null) {
            displayMode = DisplayMode.valueOf(displayModeName);
        } else {
            displayMode = DisplayMode.DROPDOWN;
        }
    }

    public void link(Model model) {
        if(!StringUtils.isEmpty(foreignKeyName)) {
            foreignKey = parent.getActualTable().findForeignKeyByName(foreignKeyName);
        } //else TODO

        if(!StringUtils.isEmpty(selectionProviderName)) {
            selectionProvider = parent.getActualTable()
                    .findSelectionProviderByName(selectionProviderName);
        } //else TODO
    }

    public void visitChildren(ModelVisitor visitor) {}

    public String getQualifiedName() {
        if(foreignKey != null) {
            return foreignKey.getQualifiedName();
        } else {
            return null;
        }
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public Crud getParent() {
        return parent;
    }

    public void setParent(Crud crud) {
        this.parent = crud;
    }

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
}
