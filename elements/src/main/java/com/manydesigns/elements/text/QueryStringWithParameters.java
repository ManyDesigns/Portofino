/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

import java.io.Serializable;
import java.util.Arrays;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class QueryStringWithParameters implements Serializable {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected final String queryString;
    protected final Object[] paramaters;

    public QueryStringWithParameters(String queryString, Object[] paramaters) {
        this.queryString = queryString;
        this.paramaters = paramaters;
    }

    public String getQueryString() {
        return queryString;
    }

    public Object[] getParameters() {
        return paramaters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryStringWithParameters that = (QueryStringWithParameters) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(paramaters, that.paramaters)) return false;
        if (queryString != null ? !queryString.equals(that.queryString) : that.queryString != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = queryString != null ? queryString.hashCode() : 0;
        result = 31 * result + (paramaters != null ? Arrays.hashCode(paramaters) : 0);
        return result;
    }
}
