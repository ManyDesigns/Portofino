/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.manydesigns.portofino.pageactions.treeview.configuration;

import com.manydesigns.elements.annotations.LabelI18N;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.PageActionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */

@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class TreeViewConfiguration implements PageActionConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************


    boolean clickableNodeNames= true;
    boolean expandable = true;
    int indent= 10;
    int treeColumn;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(TreeViewConfiguration.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public TreeViewConfiguration() {
        super();
    }

    //**************************************************************************
    // Configuration implementation
    //**************************************************************************

    public void init(Application application) {}

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlAttribute
    @Required
    @LabelI18N("treeview.configuration.clickableNodeNames")
    public boolean isClickableNodeNames() {
        return clickableNodeNames;
    }

    public void setClickableNodeNames(boolean clickableNodeNames) {
        this.clickableNodeNames = clickableNodeNames;
    }

    @XmlAttribute
    @Required
    @LabelI18N("treeview.configuration.expandable")
    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    @XmlAttribute
    @Required
    @LabelI18N("treeview.configuration.indent")
    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    @XmlAttribute
    @Required
    @LabelI18N("treeview.configuration.treeColumn")
    public int getTreeColumn() {
        return treeColumn;
    }

    public void setTreeColumn(int treeColumn) {
        this.treeColumn = treeColumn;
    }

}
