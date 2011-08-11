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

package com.manydesigns.portofino.model;

import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.model.site.RootNode;
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
            "Copyright (c) 2005-2011, ManyDesigns srl";
    public static final String JAXB_MODEL_PACKAGES = "com.manydesigns.portofino.model";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final ArrayList<Database> databases;
    protected RootNode rootNode;


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

    protected void reset() {
        // databases
        for (Database database : databases) {
            database.reset();
        }

        // site nodes
        if (rootNode != null) {
            rootNode.reset();
        }
    }

    public void init() {
        reset();
        
        // databases
        for (Database database : databases) {
            database.init(this);
        }

        // site nodes
        if (rootNode != null) {
            rootNode.init(this);
        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    @XmlElementWrapper(name="databases")
    @XmlElement(name = "database",
            type = com.manydesigns.portofino.model.datamodel.Database.class)
    public List<Database> getDatabases() {
        return databases;
    }

    @XmlElement(required = false)
    public RootNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    public Table findTableByEntityName(String entityName) {
        for(Database db : this.getDatabases()) {
            for(Schema schema : db.getSchemas()){
                for (Table tb : schema.getTables()){
                    if(entityName.equals(tb.getActualEntityName())){
                        return tb;
                    }
                }
            }

        }
        return null;
    }
}