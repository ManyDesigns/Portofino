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

package com.manydesigns.elements.text;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.Util;

import java.text.MessageFormat;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class OgnlTextFormat
        extends AbstractOgnlFormat
        implements TextFormat {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected boolean url = false;

    public static final Logger logger =
            LogUtil.getLogger(OgnlTextFormat.class);

    //**************************************************************************
    // Static initialization/methods
    //**************************************************************************

    public static OgnlTextFormat create(String ognlFormat) {
        return new OgnlTextFormat(ognlFormat);
    }

    public static String format(String expression, Object root) {
        return create(expression).format(root);
    }

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public OgnlTextFormat(String ognlFormat) {
        super(ognlFormat);
    }

    //**************************************************************************
    // TextFormat implementation
    //**************************************************************************

    public String format(Object root) {
        Object[] args = evaluateOgnlExpressions(root);
        String[] argStrings = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            String argString = (String)Util.convertValue(arg, String.class);
            argStrings[i] = url ? Util.urlencode(argString) : argString;
        }

        String result = MessageFormat.format(getFormatString(), argStrings);

        if (url) {
            result = Util.getAbsoluteUrl(result);
        }

        return result;
    }

    //**************************************************************************
    // AbstractOgnlFormat implementation
    //**************************************************************************

    protected void replaceOgnlExpression(StringBuilder sb,
                                         int index,
                                         String ognlExpression) {
        sb.append("{");
        sb.append(index);
        sb.append("}");
    }


    //**************************************************************************
    // Getters and setters
    //**************************************************************************

    public boolean isUrl() {
        return url;
    }

    public void setUrl(boolean url) {
        this.url = url;
    }
}
