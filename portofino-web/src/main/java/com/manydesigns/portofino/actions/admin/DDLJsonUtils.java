/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.database.*;

import java.util.List;

public class DDLJsonUtils {
    public static String getColumns(Table table)
    {
        StringBuffer buf = new StringBuffer();

        buf.append("{\n");
        buf.append("\"columns\": [\n");
        int i = 1;
        int size = table.getColumns().size();
        for(Column col : table.getColumns()){
            buf.append("{\n");
            printJsonValue(buf, "name", col.getColumnName(), false);
            printJsonValue(buf, "colType", col.getColumnType(), false);
            printJsonValue(buf, "javaType", col.getJavaType(), true);
            buf.append("}\n");
            if(i != size) {
                buf.append(",");
            }
            i++;
        }
        buf.append("]\n");
        buf.append("}\n");
        return buf.toString();
    }

    public static String getPk(Table table)
    {
        PrimaryKey pk = table.getPrimaryKey();
        StringBuffer buf = new StringBuffer();
        buf.append("{\n");
        printJsonValue(buf, "pkName", pk.getPrimaryKeyName(), false);
        buf.append("\"columns\": [\n");
        int i = 1;
        int size = pk.getPrimaryKeyColumns().size();
        for(PrimaryKeyColumn col : pk.getPrimaryKeyColumns()){
            buf.append("{\n");
            printJsonValue(buf, "name", col.getColumnName(), true);
            buf.append("}\n");
            if(i != size) {
                buf.append(",");
            }
            i++;
        }
        buf.append("]\n");
        buf.append("}\n");
        return buf.toString();
    }

    public static String getForeignKey(Table table)
    {
        List<ForeignKey> fks = table.getForeignKeys();
        StringBuffer buf = new StringBuffer();
        buf.append("{\n");
        buf.append("\"fks\": [\n");
        int i = 1;
        int sizeFks = fks.size();
        for(ForeignKey fk : fks){
            buf.append("{\n");
            printJsonValue(buf, "fkName", fk.getName(), false);

            buf.append("\"refs\": [\n");
            int j = 1;
            int sizeRef = fk.getReferences().size();
            for (Reference ref : fk.getReferences()){
                buf.append("{\n");
                printJsonValue(buf, "toTable", fk.getToTable().getQualifiedName(), false);
                printJsonValue(buf, "from", ref.getFromColumn(), false);
                printJsonValue(buf, "to", ref.getToColumn(), true);

                buf.append("}\n");
                if(j != sizeRef) {
                    buf.append(",");
                }
                j++;
            }
            buf.append("]\n");

            buf.append("}\n");
            if(i != sizeFks) {
                buf.append(",");
            }
            i++;
        }
        buf.append("]\n");
        buf.append("}\n");
        return buf.toString();
    }

    public static String getAnnotations(Table table)
    {
        StringBuffer buf = new StringBuffer();

        buf.append("{\n");
        buf.append("\"annotations\": [\n");
        int i = 1;
        int size = table.getAnnotations().size();
        for(Annotation ann : table.getAnnotations()){
            buf.append("{\n");
            printJsonValue(buf, "name", ann.getType(), false);
            String values = "";
            boolean first = true;
            for (String value : ann.getValues()){
                if (!first){
                    values += ", ";
                    first= false;
                }
                values += value;
            }
            printJsonValue(buf, "values", values, true);

            buf.append("}\n");
            if(i != size) {
                buf.append(",");
            }
            i++;
        }
        buf.append("]\n");
        buf.append("}\n");
        return buf.toString();
    }



    private static void printJsonValue(StringBuffer buf, String prop, String value, boolean last) {
        if (last){
            buf.append("\""+prop+"\": \""+value+"\"\n");
        } else {
            buf.append("\""+prop+"\": \""+value+"\", \n");
        }
    }
}
