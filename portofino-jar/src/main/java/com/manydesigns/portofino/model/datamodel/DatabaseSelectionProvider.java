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

package com.manydesigns.portofino.model.datamodel;

import com.manydesigns.portofino.logic.DataModelLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.xml.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(value = XmlAccessType.NONE)
public class DatabaseSelectionProvider implements ModelSelectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected final List<Reference> references;

    protected String name;
    protected String toDatabase;
    protected String toSchema;
    protected String sql;
    protected String hql;

    protected Table fromTable;
    protected Table toTable;

    //**************************************************************************
    // Support fields
    //**************************************************************************

    protected String toTableName;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(DatabaseSelectionProvider.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public DatabaseSelectionProvider() {
        references = new ArrayList<Reference>();
    }

    public DatabaseSelectionProvider(Table fromTable) {
        this();
        this.fromTable = fromTable;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        fromTable = (Table) parent;
    }

    public void reset() {
        toTable = null;
    }

    public void init(Model model) {
        assert name != null;
        assert toDatabase != null;
        assert toSchema != null;
        assert toTableName != null;

        String qualifiedToTableName =
                Table.composeQualifiedName(toDatabase, toSchema, toTableName);
        toTable = DataModelLogic.findTableByQualifiedName(model, qualifiedToTableName);
        if (toTable == null) {
            logger.warn("Cannot find destination table '{}'", qualifiedToTableName);
        }
    }

    public String getQualifiedName() {
        return name;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************
    @XmlElementWrapper(name="references")
    @XmlElement(name="reference",type=Reference.class)
    public List<Reference> getReferences() {
        return references;
    }

    @Identifier
    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(required = true)
    public String getToDatabase() {
        return toDatabase;
    }

    public void setToDatabase(String toDatabase) {
        this.toDatabase = toDatabase;
    }

    @XmlAttribute(required = true)
    public String getToSchema() {
        return toSchema;
    }

    public void setToSchema(String toSchema) {
        this.toSchema = toSchema;
    }

    @XmlAttribute(required = false)
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @XmlAttribute(required = false)
    public String getHql() {
        return hql;
    }

    public void setHql(String hql) {
        this.hql = hql;
    }

    public Table getFromTable() {
        return fromTable;
    }

    public void setFromTable(Table fromTable) {
        this.fromTable = fromTable;
    }

    public Table getToTable() {
        return toTable;
    }

    public void setToTable(Table toTable) {
        this.toTable = toTable;
    }

    @XmlAttribute(name = "toTable")
    public String getToTableName() {
        return toTableName;
    }

    public void setToTableName(String toTableName) {
        this.toTableName = toTableName;
    }
}
