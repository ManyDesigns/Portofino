/*
 * Copyright (C) 2005-2009 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.composites;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.Element;
import com.manydesigns.elements.xml.XhtmlBuffer;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Wizard extends AbstractReflectiveCompositeElement {
    public static final String copyright =
            "Copyright (c) 2005-2009, ManyDesigns srl";

    public static final String STEP_SUFFIX = ".step";

    protected int _step;

    //--------------------------------------------------------------------------
    // Costruttori
    //--------------------------------------------------------------------------
    public Wizard() {
        super();
        _step = 0;
    }

    //--------------------------------------------------------------------------
    // Implementazione di Element
    //--------------------------------------------------------------------------
    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);
        String stepStr = req.getParameter(_id + STEP_SUFFIX);
        try {
            _step = Integer.parseInt(stepStr);
        } catch (Exception e) {
            _step = 0;
        }
    }

    public boolean validate() {
        int index = 0;
        for (Element component : elements()) {
            if (index == _step) {
                return component.validate();
            }
            index++;
        }
        return true;
    }

    public void toXhtml(XhtmlBuffer xb) {
        int index = 0;
        for (Element component : elements()) {
            if (index == _step) {
                component.setMode(_mode);
                component.toXhtml(xb);
            } else {
                component.setMode(Mode.HIDDEN);
                component.toXhtml(xb);
            }
            index++;
        }
        xb.writeInputHidden(_id + STEP_SUFFIX, Integer.toString(_step));
    }

    //--------------------------------------------------------------------------
    // Altri metodi
    //--------------------------------------------------------------------------
    public boolean hasNext() {
        return _step < _fields.size() - 1;
    }

    public boolean hasPrevious() {
        return _step > 0;
    }

    public void next() {
        _step++;
        if (_step >= _fields.size()) {
            _step = _fields.size() - 1;
        }
    }

    public void previous() {
        _step--;
        if (_step < 0) {
            _step = 0;
        }
    }
}
