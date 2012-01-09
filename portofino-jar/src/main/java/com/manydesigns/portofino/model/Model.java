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

import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.model.datamodel.ConnectionProvider;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.pages.RootPage;
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
    protected RootPage rootPage;


    public static final Logger logger = LoggerFactory.getLogger(Model.class);

    //**************************************************************************
    // Constructors and init
    //**************************************************************************

    public Model() {
        this.databases = new ArrayList<Database>();
    }

    public void initDatabases(Application application) {
        for(Database database : getDatabases()) {
            ConnectionProvider connectionProvider = database.getConnectionProvider();
            if(connectionProvider != null) {
                connectionProvider.reset();
                connectionProvider.init(application);
            } else {
                logger.error("No connection provider specified for {}", database);
            }
        }
    }

    //**************************************************************************
    // Reset / init
    //**************************************************************************

    protected class ResetVisitor extends ModelVisitor {

        @Override
        public void visitNodeBeforeChildren(ModelObject node) {
            node.reset();
        }
    }

    protected class InitVisitor extends ModelVisitor {

        @Override
        public void visitNodeBeforeChildren(ModelObject node) {
            node.init(Model.this);
        }
    }

    protected class LinkVisitor extends ModelVisitor {

        @Override
        public void visitNodeBeforeChildren(ModelObject node) {
            node.link(Model.this);
        }
    }

    protected void reset() {
        visit(new ResetVisitor());
    }

    public void init() {
        reset();
        visit(new InitVisitor());
        visit(new LinkVisitor());
    }

    public void init(ModelObject rootObject) {
        new ResetVisitor().visit(rootObject);
        new InitVisitor().visit(rootObject);
        new LinkVisitor().visit(rootObject);
    }

    protected void visit(ModelVisitor visitor) {
        // databases
        for (Database database : databases) {
            visitor.visit(database);
        }

        // pages
        if (rootPage != null) {
            visitor.visit(rootPage);
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
    public RootPage getRootPage() {
        return rootPage;
    }

    public void setRootPage(RootPage rootPage) {
        this.rootPage = rootPage;
    }

}