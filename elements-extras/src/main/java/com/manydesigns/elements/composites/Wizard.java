/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.composites;

import com.manydesigns.elements.Element;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Wizard extends AbstractReflectiveCompositeElement {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final String STEP_SUFFIX = ".step";

    protected int _step;

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public Wizard() {
        super();
        _step = 0;
    }

    //**************************************************************************
    // Implementazione di Element
    //**************************************************************************
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

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        int index = 0;
        for (Element component : elements()) {
            if (index == _step) {
                component.toXhtml(xb);
            } else {
                component.toXhtml(xb);
            }
            index++;
        }
        xb.writeInputHidden(_id + STEP_SUFFIX, Integer.toString(_step));
    }

    //**************************************************************************
    // Altri metodi
    //**************************************************************************
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
