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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.reflection.PropertyAccessor;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class CodiceFiscaleField extends RegExpTextField {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    // regex per codice fiscale con gestione omocodie.
    public final static String codiceFiscaleRegExp =
            "[A-Z]{6}[0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]";

    /* Regex piu' semplice che non gestisce le omocodie e':
     * [A-Z]{6}[\\d]{2}[A-Z][\\d]{2}[A-Z][\\d]{3}[A-Z]
     */
    //**************************************************************************
    // Constructors
    //**************************************************************************
    public CodiceFiscaleField(PropertyAccessor accessor, Mode mode, String prefix) {
        super(accessor, mode, prefix, codiceFiscaleRegExp);
        setErrorString(getText("elements.error.field.codice.fiscale.format"));
        setAutoCapitalize(true);
    }
}
