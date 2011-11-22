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

package com.manydesigns.portofino.buttons;

import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.pages.Page;
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ButtonsLogic {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static class ButtonComparatorByOrder implements Comparator<ButtonInfo> {
        public int compare(ButtonInfo o1, ButtonInfo o2) {
            return Double.compare(o1.getButton().order(), o2.getButton().order());
        }
    }

    public static List<ButtonInfo> getButtonsForClass
            (Class<?> someClass, String list, List<String> groups, Page page) {
        List<ButtonInfo> buttons = new ArrayList<ButtonInfo>();
        for(Method method : someClass.getMethods()) {
            Button button = getButtonForMethod(method, list);
            if(button != null) {
                ButtonInfo buttonInfo = new ButtonInfo(button,method, someClass);
                buttons.add(buttonInfo);
            }
        }
        Collections.sort(buttons, new ButtonComparatorByOrder());
        return buttons;
    }

    public static Button getButtonForMethod(Method method, String list) {
        Button button = method.getAnnotation(Button.class);
        if(button != null && list.equals(button.list())) {
            return button;
        } else {
            Buttons buttons = method.getAnnotation(Buttons.class);
            if(buttons != null) {
                for(Button b : buttons.value()) {
                    if(list.equals(b.list())) {
                        return b;
                    }
                }
            }
        }
        return null;
    }

    public static boolean hasPermissions
            (@NotNull ButtonInfo button, @NotNull Page page, @NotNull Collection<String> groupIds) {
        RequiresPermissions requiresPermissions =
                    SecurityLogic.getRequiresPermissionsAnnotation(button.getMethod(), button.getFallbackClass());
        if(requiresPermissions != null) {
            return SecurityLogic.hasPermissions(page.getPermissions(), groupIds, requiresPermissions);
        } else {
            return true;
        }
    }

}
