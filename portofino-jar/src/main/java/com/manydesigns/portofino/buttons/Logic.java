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

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Logic {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static class ButtonComparatorByOrder implements Comparator<ButtonInfo> {
        public int compare(ButtonInfo o1, ButtonInfo o2) {
            return Double.compare(o1.getButton().order(), o2.getButton().order());
        }
    }

    public static Map<String, List<ButtonInfo>> getButtonsForClass(Class<?> someClass) {
        Map<String, List<ButtonInfo>> buttonsMap = new HashMap<String, List<ButtonInfo>>();
        for(Method method : someClass.getMethods()) {
            for(ButtonInfo button : getButtonsForMethod(method)) {
                List<ButtonInfo> buttons = buttonsMap.get(button.getButton().list());
                if(buttons == null) {
                    buttons = new ArrayList<ButtonInfo>();
                    buttonsMap.put(button.getButton().list(), buttons);
                }
                buttons.add(button);
            }
        }
        for(List<ButtonInfo> buttons : buttonsMap.values()) {
            Collections.sort(buttons, new ButtonComparatorByOrder());
        }
        return buttonsMap;
    }

    private static Iterable<? extends ButtonInfo> getButtonsForMethod(Method method) {
        List<ButtonInfo> buttonsList = new ArrayList<ButtonInfo>();
        Button button = method.getAnnotation(Button.class);
        if(button != null) {
            buttonsList.add(new ButtonInfo(getEventName(method), button));
        } else {
            Buttons buttons = method.getAnnotation(Buttons.class);
            if(buttons != null) {
                for(Button b : buttons.value()) {
                    buttonsList.add(new ButtonInfo(getEventName(method), b));
                }
            }
        }
        return buttonsList;
    }

    private static String getEventName(Method method) {
        return method.getName();
    }

}
