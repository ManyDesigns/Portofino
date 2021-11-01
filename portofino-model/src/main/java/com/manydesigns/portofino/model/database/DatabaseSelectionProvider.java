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

package com.manydesigns.portofino.model.database;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObjectVisitor;
import com.manydesigns.portofino.model.Named;
import com.manydesigns.portofino.model.Unmarshallable;
import com.manydesigns.portofino.model.database.annotations.SelectionProvider;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(value = XmlAccessType.NONE)
@XmlType(propOrder = {"name","toDatabase","references","hql", "sql"})
public class DatabaseSelectionProvider implements ModelSelectionProvider, Named, Unmarshallable {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected final List<Reference> references;

    protected Table fromTable;
    protected EAnnotation annotation;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(DatabaseSelectionProvider.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public DatabaseSelectionProvider() {
        references = new ArrayList<>();
        annotation = EcoreFactory.eINSTANCE.createEAnnotation();
        annotation.setSource(SelectionProvider.class.getName());
    }

    public DatabaseSelectionProvider(Table fromTable) {
        this();
        setFromTable(fromTable);
    }

    public DatabaseSelectionProvider(Table fromTable, EAnnotation annotation) {
        this();
        this.annotation = annotation;
        setFromTable(fromTable);
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void setParent(Object parent) {
        setFromTable((Table) parent);
    }

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        setFromTable((Table) parent);
    }

    public void reset() {}

    public void init(Model model, Configuration configuration) {
        assert fromTable != null;
        if(getName() == null) {
            throw new RuntimeException("name is required. Parent: " + fromTable.getQualifiedName());
        }
        if(getToDatabase() == null) {
            if(fromTable.getSchema() != null) {
                setToDatabase(fromTable.getSchema().getDatabaseName());
            }
            if(getToDatabase() == null) {
                throw new RuntimeException("toDatabase is required. Parent: " + fromTable.getQualifiedName());
            }
        }
    }

    public void link(Model model, Configuration configuration) {}

    @Override
    public void afterLink(Model model, Configuration configuration) {
        annotation.getDetails().put("properties", references.stream().map(r -> {
            Column column = r.getActualFromColumn();
            if(column == null) {
                return r.fromPropertyName;
            } else {
                return column.getActualPropertyName();
            }
        }).collect(Collectors.joining(", ")));
    }

    public void visitChildren(ModelObjectVisitor visitor) {
        for (Reference reference : references) {
            visitor.visit(reference);
        }
    }

    public String getQualifiedName() {
        return getName();
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************
    @XmlElementWrapper(name="references")
    @XmlElement(name="reference", type = Reference.class)
    public List<Reference> getReferences() {
        return references;
    }

    @XmlAttribute(required = true)
    public String getName() {
        return annotation.getDetails().get("name");
    }

    public void setName(String name) {
        annotation.getDetails().put("name", name);
    }

    @XmlAttribute(required = true)
    public String getToDatabase() {
        return annotation.getDetails().get("database");
    }

    public void setToDatabase(String toDatabase) {
        annotation.getDetails().put("database", toDatabase);
    }

    @XmlAttribute()
    public String getSql() {
        return "SQL".equals(annotation.getDetails().get("language")) ? annotation.getDetails().get("query") : null;
    }

    public void setSql(String sql) {
        if(StringUtils.isNotBlank(sql) || "SQL".equals(annotation.getDetails().get("language"))) {
            annotation.getDetails().put("query", sql);
            annotation.getDetails().put("language", "SQL");
        }
    }

    @XmlAttribute()
    public String getHql() {
        return "HQL".equals(annotation.getDetails().get("language")) ? annotation.getDetails().get("query") : null;
    }

    public void setHql(String hql) {
        if(StringUtils.isNotBlank(hql) || "HQL".equals(annotation.getDetails().get("language"))) {
            annotation.getDetails().put("query", hql);
            annotation.getDetails().put("language", "HQL");
        }
    }

    public Table getFromTable() {
        return fromTable;
    }

    public void setFromTable(Table fromTable) {
        this.fromTable = fromTable;
        fromTable.getModelElement().getEAnnotations().add(annotation);
    }

    public Table getToTable() {
        return null;
    }

    @Override
    public EModelElement getModelElement() {
        return annotation;
    }

    @Override
    public String toString() {
        return MessageFormat.format("selection provider {0}", getQualifiedName());
    }
}
