/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.model.usecases;

import com.manydesigns.portofino.model.annotations.Annotation;
import com.manydesigns.portofino.model.datamodel.Table;

import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UseCase {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";
    

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String name;
    protected String title;
    protected String tableName;
    protected Table table;
    protected String filter;
    protected final List<UseCaseProperty> properties;
    protected final List<Annotation> annotations;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public UseCase(String name, String title, String tableName, String filter) {
        this.name = name;
        this.title = title;
        this.tableName = tableName;
        this.filter = filter;
        properties = new ArrayList<UseCaseProperty>();
        annotations = new ArrayList<Annotation>();
    }


    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public List<UseCaseProperty> getProperties() {
        return properties;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
