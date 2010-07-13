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

package com.manydesigns.portofino.actions;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.hyperlinks.ExpressionHyperlinkGenerator;
import com.manydesigns.elements.composites.ClassTableFormBuilder;
import com.manydesigns.elements.composites.TableForm;
import com.manydesigns.portofino.base.context.MDContext;
import com.manydesigns.portofino.base.context.ModelObjectNotFoundException;
import com.manydesigns.portofino.base.model.Table;
import com.manydesigns.portofino.base.model.Column;
import com.manydesigns.portofino.base.reflection.TableAccessor;
import com.manydesigns.portofino.interceptors.MDContextAware;
import com.opensymphony.xwork2.ActionSupport;

import java.util.List;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Search extends ActionSupport implements MDContextAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public MDContext context;

    public void setContext(MDContext context) {
        this.context = context;
    }

    
    public String qualifiedTableName;
    public Table table;
    public List<Map<String, Object>> objects;
    public TableForm tableForm;

    public String execute() throws ModelObjectNotFoundException {
        table = context.findTableByQualifiedName(qualifiedTableName);
        objects = context.getAllObjects(qualifiedTableName);

        TableAccessor accessor = new TableAccessor(table);

        String readLinkExpression = getReadLinkExpression();
        ExpressionHyperlinkGenerator generator =
                new ExpressionHyperlinkGenerator(
                        accessor, readLinkExpression, "dummy-alt");

        ClassTableFormBuilder tableFormBuilder =
                new ClassTableFormBuilder(accessor)
                        .configNRows(objects.size());

        // ogni colonna chiave primaria sarà clickabile
        for (Column column : table.getPrimaryKey().getColumns()) {
            tableFormBuilder.configHyperlinkGenerator(
                    column.getColumnName(), generator);
        }

        tableForm = tableFormBuilder.build();
        tableForm.setMode(Mode.VIEW);
        tableForm.readFromObject(objects);

        return SUCCESS;
    }

    private String getReadLinkExpression() {
        StringBuilder sb = new StringBuilder("/");
        sb.append(table.getQualifiedName());
        sb.append("/Read.action?pk=");
        boolean first = true;
        for (Column column : table.getPrimaryKey().getColumns()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("{");
            sb.append(column.getColumnName());
            sb.append("}");
        }
        return sb.toString();
    }

}
