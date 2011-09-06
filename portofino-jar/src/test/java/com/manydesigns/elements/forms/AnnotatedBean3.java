/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.elements.forms;

import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.fields.search.SelectSearchField;
import com.manydesigns.elements.options.DisplayMode;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class AnnotatedBean3 {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    @Select(values={"1", "2"},labels={"a", "b"})
    public String field1;
    public String field2;
    @Select(values={"1", "2"},labels={"a", "b"}, displayMode = DisplayMode.AUTOCOMPLETE, searchDisplayMode = SelectSearchField.DisplayMode.AUTOCOMPLETE)
    public String field3;
    @Select(values={"1", "2"},labels={"a", "b"}, displayMode = DisplayMode.RADIO, searchDisplayMode = SelectSearchField.DisplayMode.RADIO)
    public String field4;
    @Select(values={"1", "2"},labels={"a", "b"}, displayMode = DisplayMode.RADIO, searchDisplayMode = SelectSearchField.DisplayMode.CHECKBOX)
    public String field5;
    @Select(values={"1", "2"},labels={"a", "b"}, displayMode = DisplayMode.RADIO, searchDisplayMode = SelectSearchField.DisplayMode.MULTIPLESELECT)
    public String field6;
}
