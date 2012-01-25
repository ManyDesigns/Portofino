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

package com.manydesigns.portofino.model.database;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelObjectVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class PrimaryKeyColumn implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************
    protected PrimaryKey primaryKey;
    protected String columnName;
    protected Generator generator;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected Column actualColumn;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(PrimaryKeyColumn.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public PrimaryKeyColumn() {}

    public PrimaryKeyColumn(PrimaryKey primaryKey) {
        this();
        this.primaryKey = primaryKey;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        primaryKey = (PrimaryKey) parent;
    }

    public void reset() {
        actualColumn = null;
    }

    public void link(Model model) {}

    public void visitChildren(ModelObjectVisitor visitor) {}

    public void init(Model model) {
        assert primaryKey != null;
        assert columnName != null;

        actualColumn = primaryKey.getTable().findColumnByName(columnName);
        if (actualColumn == null) {
            logger.warn("Cannor wire primary key column '{}' to primary key '{}'",
                    columnName, primaryKey);

        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    @XmlAttribute(required = true)
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Column getActualColumn() {
        return actualColumn;
    }


    @XmlElements({
          @XmlElement(name="sequenceGenerator",type=SequenceGenerator.class),
          @XmlElement(name="incrementGenerator",type=IncrementGenerator.class),
          @XmlElement(name="tableGenerator",type=TableGenerator.class)
    })
    public Generator getGenerator() {
        return generator;
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }
}
