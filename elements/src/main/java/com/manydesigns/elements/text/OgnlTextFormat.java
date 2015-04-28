/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.text;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Locale;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class OgnlTextFormat
        extends AbstractOgnlFormat
        implements TextFormat {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected boolean url = false;
    protected String encoding = "ISO-8859-1";
    protected Locale locale;

    public static final Logger logger =
            LoggerFactory.getLogger(OgnlTextFormat.class);

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
            String argString = (String) OgnlUtils.convertValue(arg, String.class);
            if (argString == null) {
                argStrings[i] = null;
            } else {
                try {
                    argStrings[i] = url ? URLEncoder.encode(argString, encoding) : argString;
                } catch (UnsupportedEncodingException e) {
                    throw new Error(e);
                }
            }
        }

        Locale locale = this.locale;
        if(locale == null) {
            HttpServletRequest req =
                ElementsThreadLocals.getHttpServletRequest();
            if(req != null) {
                locale = req.getLocale();
            } else { //Not in a HTTP request.
                locale = Locale.getDefault();
            }
        }
        String result = new MessageFormat(getFormatString(), locale).format(argStrings);

        if (url) {
            result = Util.getAbsoluteUrl(result);
        }

        return result;
    }

    //**************************************************************************
    // AbstractOgnlFormat implementation
    //**************************************************************************


    @Override
    protected String escapeText(String text) {
        return text.replace("'", "''").replace("{", "'{'");
    }

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

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
