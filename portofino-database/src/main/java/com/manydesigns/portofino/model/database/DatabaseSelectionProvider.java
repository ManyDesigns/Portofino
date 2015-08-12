/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.model.database;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObjectVisitor;
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
@XmlType(propOrder = {"name","toDatabase","references","hql", "sql"})
public class DatabaseSelectionProvider implements ModelSelectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    protected final List<Reference> references;

    protected String name;
    protected String toDatabase;
    protected String sql;
    protected String hql;

    protected Table fromTable;

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

    public void reset() {}

    public void init(Model model) {
        assert name != null;
        assert toDatabase != null;
    }

    public void link(Model model) {}

    public void visitChildren(ModelObjectVisitor visitor) {
        for (Reference reference : references) {
            visitor.visit(reference);
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
        return null;
    }

}
