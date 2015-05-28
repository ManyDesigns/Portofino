/*
* Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.util;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class BootstrapSizes {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    /**
     * These no longer work in Bootstrap 3.
     */
    @Deprecated
    public static final String
        MINI = "input-mini", SMALL = "input-sm", MEDIUM = "input-medium",
        LARGE = "input-lg", XLARGE = "input-xlarge", XXLARGE = "input-xxlarge";

    public static final String FILL_ROW = "fill-row";
    /**
     * Use fill-row instead, and note that it only works for 1-column horizontal forms (because in other form
     * configurations input fields already fill all available space).
     */
    @Deprecated
    public static final String BLOCK_LEVEL = FILL_ROW;

    public static final String
        COL_SM_1 = "col-sm-1", COL_SM_2 = "col-sm-2", COL_SM_3 = "col-sm-3", COL_SM_4 = "col-sm-4",
        COL_SM_5 = "col-sm-5", COL_SM_6 = "col-sm-6", COL_SM_7 = "col-sm-7", COL_SM_8 = "col-sm-8",
        COL_SM_9 = "col-sm-9", COL_SM_10 = "col-sm-10", COL_SM_11 = "col-sm-11", COL_SM_12 = "col-sm-12";

    /**
     * @deprecated Use COL_SM_* instead.
     */
    @Deprecated
    public static final String
        SPAN1 = COL_SM_1, SPAN2 = COL_SM_2, SPAN3 = COL_SM_3, SPAN4 = COL_SM_4,
        SPAN5 = COL_SM_5, SPAN6 = COL_SM_6, SPAN7 = COL_SM_7, SPAN8 = COL_SM_8,
        SPAN9 = COL_SM_9, SPAN10 = COL_SM_10, SPAN11 = COL_SM_11, SPAN12 = COL_SM_12;
}
