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

package com.manydesigns.portofino.actions.model;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.portofino.actions.AbstractCrudAction;
import com.manydesigns.portofino.actions.RelatedTableForm;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.datamodel.ForeignKey;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.util.PkHelper;
import org.apache.struts2.interceptor.ServletRequestAware;

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableDataAction extends AbstractCrudAction
        implements ServletRequestAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";


    @Override
    public void setupMetadata() {
        baseTable = model.findTableByQualifiedName(qualifiedName);
        classAccessor = context.getTableAccessor(qualifiedName);
        pkHelper = new PkHelper(classAccessor);
        if (classAccessor == null) {
            throw new ModelObjectNotFoundError(qualifiedName);
        }
    }


    public String redirectToFirst() {
        List<Table> tables = model.getAllTables();
        if (tables.isEmpty()) {
            return NO_CLASSES;
        } else {
            qualifiedName = tables.get(0).getQualifiedName();
            return REDIRECT_TO_FIRST;
        }
    }

    
    protected void setupRelatedTableForm(ForeignKey relationship) {
        List<Object> relatedObjects =
                context.getRelatedObjects(qualifiedName, object,
                        relationship.getForeignKeyName());

        String qualifiedFromTableName =
                relationship.getFromTable().getQualifiedName();
        TableAccessor relatedTableAccessor =
                context.getTableAccessor(qualifiedFromTableName);
        TableFormBuilder tableFormBuilder =
                new TableFormBuilder(relatedTableAccessor);
        tableFormBuilder.configNRows(relatedObjects.size());
        TableForm tableForm = tableFormBuilder
                .configMode(Mode.VIEW)
                .build();
        tableForm.readFromObject(relatedObjects);

        RelatedTableForm relatedTableForm =
                new RelatedTableForm(relationship, tableForm, relatedObjects);
        relatedTableFormList.add(relatedTableForm);
    }

}
