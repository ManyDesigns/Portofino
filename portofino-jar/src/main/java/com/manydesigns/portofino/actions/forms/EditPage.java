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

package com.manydesigns.portofino.actions.forms;

import com.manydesigns.elements.annotations.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class EditPage {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    @LabelI18N("com.manydesigns.portofino.actions.forms.EditPage.id")
    @Updatable(false)
    public String id;

    @LabelI18N("com.manydesigns.portofino.actions.forms.EditPage.description")
    @Required
    @Multiline
    public String description;

    /*@LabelI18N("com.manydesigns.portofino.actions.forms.EditPage.embedInParent")
    public boolean embedInParent;

    @LabelI18N("com.manydesigns.portofino.actions.forms.EditPage.showInNavigation")
    public boolean showInNavigation;*/

    @LabelI18N("com.manydesigns.portofino.actions.forms.EditPage.subtreeRoot")
    public boolean subtreeRoot;

    @LabelI18N("com.manydesigns.portofino.actions.forms.EditPage.layout")
    @Required
    public String layout;

    @LabelI18N("com.manydesigns.portofino.actions.forms.EditPage.detailLayout")
    @Required
    public String detailLayout;

}
