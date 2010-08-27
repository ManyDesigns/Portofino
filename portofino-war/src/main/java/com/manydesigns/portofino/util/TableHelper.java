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

package com.manydesigns.portofino.util;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.reflection.helpers.ClassAccessorManager;
import com.manydesigns.elements.text.ExpressionGenerator;
import com.manydesigns.elements.text.Generator;
import com.manydesigns.portofino.model.datamodel.Column;
import com.manydesigns.portofino.model.datamodel.Table;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableHelper {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public Generator createKeyGenerator(Table table) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Column column : table.getPrimaryKey().getColumns()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("%{");
            sb.append(column.getPropertyName());
            sb.append("}");
        }
        return ExpressionGenerator.create(sb.toString());
    }

    public HashMap<String, Object> parsePkString(Table table, String pkString) {
        String[] pkList = StringUtils.split(pkString,",");

        int i = 0;
        HashMap<String, Object> pkMap = new HashMap<String, Object>();

        pkMap.put("$type$", table.getQualifiedName());

        for(Column column : table.getPrimaryKey().getColumns() ) {
            String stringValue = pkList[i];
            Object value = ConvertUtils.convert(
                    stringValue, column.getJavaType());
            pkMap.put(column.getPropertyName(), value);
            i++;
        }

        return pkMap;
    }

    public String generatePkString(Table table, Object object) {
        ClassAccessor classAccessor =
                ClassAccessorManager.getManager().tryToInstantiateFromClass(table);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(Column column : table.getPrimaryKey().getColumns() ) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            try {
                PropertyAccessor propertyAccessor =
                        classAccessor.getProperty(column.getPropertyName());
                Object value = propertyAccessor.get(object);
                String stringValue = ConvertUtils.convert(value);
                sb.append(stringValue);
            } catch (Throwable e) {
                e.printStackTrace();  // TODO: sistemare
            }
        }
        return sb.toString();
    }


}
