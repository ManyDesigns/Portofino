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

package com.manydesigns.portofino.model.selectionproviders;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/

@XmlAccessorType(value = XmlAccessType.NONE)
public class SelectionProperty implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";


    //**************************************************************************
    // Fields
    //**************************************************************************

    protected ModelSelectionProvider modelSelectionProvider;

    protected String name;


    //**************************************************************************
    // Constructors
    //**************************************************************************


    public SelectionProperty() {}

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        modelSelectionProvider = (ModelSelectionProvider) parent;
    }

    public void reset() {}

    public void init(Model model) {
        assert modelSelectionProvider != null;
        assert name != null;
    }

    public String getQualifiedName() {
        return String.format("%s.%s",
                modelSelectionProvider.getQualifiedName(), name);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModelSelectionProvider getModelSelectionProvider() {
        return modelSelectionProvider;
    }

    public void setModelSelectionProvider(ModelSelectionProvider modelSelectionProvider) {
        this.modelSelectionProvider = modelSelectionProvider;
    }
}
