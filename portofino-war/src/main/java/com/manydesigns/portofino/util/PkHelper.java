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

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.ExpressionGenerator;
import com.manydesigns.elements.text.Generator;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;

import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class PkHelper {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    public final static Logger logger = LogUtil.getLogger(PkHelper.class);

    protected final ClassAccessor classAccessor;


    //**************************************************************************
    // Constructor
    //**************************************************************************

    public PkHelper(ClassAccessor classAccessor) {
        this.classAccessor = classAccessor;
    }


    //**************************************************************************
    // Methods
    //**************************************************************************

    public Generator createPkGenerator() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (PropertyAccessor property : classAccessor.getKeyProperties()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("%{");
            sb.append(property.getName());
            sb.append("}");
        }
        return ExpressionGenerator.create(sb.toString());
    }

    public Object parsePkString(String pkString) {
        String[] pkList = StringUtils.split(pkString, ",");

        int i = 0;
        Object result = classAccessor.newInstance();

        for(PropertyAccessor property : classAccessor.getKeyProperties()) {
            String stringValue = pkList[i];
            Object value = ConvertUtils.convert(
                    stringValue, property.getType());
            try {
                property.set(result, value);
            } catch (Throwable e) {
                LogUtil.warningMF(logger,
                        "Could not set property: {0}", e, property);
                return null;
            }
            i++;
        }

        return result;
    }

    public String generatePkString(Object object) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(PropertyAccessor property : classAccessor.getKeyProperties()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            try {
                Object value = property.get(object);
                String stringValue = ConvertUtils.convert(value);
                sb.append(stringValue);
            } catch (Throwable e) {
                LogUtil.warningMF(logger,
                        "Could not get property: {0}", e, property);
                return null;
            }
        }
        return sb.toString();
    }


}
