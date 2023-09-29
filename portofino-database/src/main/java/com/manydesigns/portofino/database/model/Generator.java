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

package com.manydesigns.portofino.database.model;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelObjectVisitor;
import com.manydesigns.portofino.model.Unmarshallable;
import org.apache.commons.configuration2.Configuration;
import org.eclipse.emf.ecore.EModelElement;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * Configures a generator for a primary key column.
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(value = XmlAccessType.NONE)
public abstract class Generator implements ModelObject, Unmarshallable {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";
    //**************************************************************************
    // Fields
    //**************************************************************************
    protected PrimaryKeyColumn primaryKeyColumn;


    //**************************************************************************
    // Constructor
    //**************************************************************************

    public Generator() {}

    protected Generator(PrimaryKeyColumn primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************

    public PrimaryKeyColumn getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(PrimaryKeyColumn primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    //**************************************************************************
    // DatamodelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        primaryKeyColumn = (PrimaryKeyColumn) parent;
    }

    public void setParent(Object parent) {
        primaryKeyColumn = (PrimaryKeyColumn) parent;
    }

    public void reset() {}

    public void init(Object context, Configuration configuration) {
        assert primaryKeyColumn != null;
    }

    public void link(Object context, Configuration configuration) {}

    public void visitChildren(ModelObjectVisitor visitor) {}

    @Override
    public EModelElement getModelElement() {
        return null;
    }
}
