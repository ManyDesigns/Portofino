/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.manydesigns.portofino.actions.model;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Multiline;
import org.apache.commons.lang.text.StrTokenizer;

import java.util.Map;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class AnnModel {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";
    public String columnName;
    public String annotationName;
    public Properties properties;

    public AnnModel() {
        properties = new Properties();
    }

    @Multiline
    @Label("Properties")
    public String getPropValues(){
        StringBuffer result = new StringBuffer();

        for (Map.Entry entry : properties.entrySet()){
            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
            result.append(";\n");
        }

        return result.toString();
    }

    public void setPropValues(String propValues){
        properties.clear();
        StrTokenizer strTokenizer = new StrTokenizer(propValues, ";");
        for (String property : strTokenizer.getTokenArray()){
                StrTokenizer strTokenizer2 = new StrTokenizer(property, "=");
                String key = strTokenizer2.getTokenArray()[0];
                String value = strTokenizer2.getTokenArray()[1];
                properties.put(key, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnModel annModel = (AnnModel) o;

        if (annotationName != null ? !annotationName.equals(annModel.annotationName) : annModel.annotationName != null)
            return false;
        if (columnName != null ? !columnName.equals(annModel.columnName) : annModel.columnName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = columnName != null ? columnName.hashCode() : 0;
        result = 31 * result + (annotationName != null ? annotationName.hashCode() : 0);
        return result;
    }
}
