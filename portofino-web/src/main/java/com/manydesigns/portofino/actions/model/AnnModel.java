/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2013, ManyDesigns srl";
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
