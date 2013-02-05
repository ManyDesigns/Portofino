/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.model;

import com.manydesigns.portofino.model.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Model {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";
    public static final String JAXB_MODEL_PACKAGES = "com.manydesigns.portofino.model";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final ArrayList<Database> databases;

    public static final Logger logger = LoggerFactory.getLogger(Model.class);

    //**************************************************************************
    // Constructors and init
    //**************************************************************************

    public Model() {
        this.databases = new ArrayList<Database>();
    }

    //**************************************************************************
    // Reset / init
    //**************************************************************************

    public void init() {
        for (Database database : databases) {
            init(database);
        }
    }

    public void init(ModelObject rootObject) {
        new ResetVisitor().visit(rootObject);
        new InitVisitor(this).visit(rootObject);
        new LinkVisitor(this).visit(rootObject);
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    @XmlElementWrapper(name="databases")
    @XmlElement(name = "database",
            type = com.manydesigns.portofino.model.database.Database.class)
    public List<Database> getDatabases() {
        return databases;
    }

}