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

package com.manydesigns.elements;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Util {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static String getAbsoluteLink(HttpServletRequest req,
                                         String link) {
        StringBuilder sb = new StringBuilder();

        if (!"/".equals(req.getContextPath())) {
            sb.append(req.getContextPath());
        }

        if (!link.startsWith("/")) {
            sb.append("/");
        }

        sb.append(link);

        return sb.toString();
    }

    public static String getAbsoluteUrl(String link) {
        HttpServletRequest req =
                ElementsThreadLocals.getHttpServletRequest();

        return getAbsoluteLink(req, link);
    }

    public static String camelCaseToWords(String s) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (first) {
                first = false;
                sb.append(Character.toUpperCase(c));
            } else {
                if (Character.isUpperCase(c)) {
                    sb.append(' ');
                    sb.append(Character.toLowerCase(c));
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }


    public static String urlencode(String s) {
        if (s == null) {
            return null;
        } else {
            try {
                return URLEncoder.encode(s, "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                throw new Error(e);
            }
        }
    }
}
