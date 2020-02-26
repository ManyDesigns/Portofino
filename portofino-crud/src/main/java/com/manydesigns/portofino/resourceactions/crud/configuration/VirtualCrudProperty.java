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

package com.manydesigns.portofino.resourceactions.crud.configuration;

import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.model.Model;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/

@XmlAccessorType(value = XmlAccessType.NONE)
@XmlType(propOrder = {"expression", "typeName","language"})
public class VirtualCrudProperty extends CrudProperty {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected Class<?> type;
    protected String typeName;
    protected String expression;
    protected String language = "OGNL";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(VirtualCrudProperty.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public VirtualCrudProperty() {}

    //**************************************************************************
    // Configuration implementation
    //**************************************************************************

    public void init(Model model, Configuration configuration) {
        super.init(model, configuration);
        type = ReflectionUtil.loadClass(typeName);
    }

    @XmlAttribute(required = true)
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @XmlAttribute
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @XmlAttribute(name = "type", required = true)
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @XmlTransient
    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    @XmlTransient
    public boolean isSearchable() {
        return super.isSearchable();
    }

    @Override
    public void setSearchable(boolean searchable) {
        super.setSearchable(searchable);
    }
}
