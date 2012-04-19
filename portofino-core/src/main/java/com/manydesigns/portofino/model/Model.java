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
            "Copyright (c) 2005-2012, ManyDesigns srl";
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