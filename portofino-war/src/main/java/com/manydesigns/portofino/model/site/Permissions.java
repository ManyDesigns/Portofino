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

package com.manydesigns.portofino.model.site;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import org.apache.commons.collections.CollectionUtils;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class Permissions implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<String> allow;
    protected final List<String> deny;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Permissions() {
        allow = new ArrayList<String>();
        deny = new ArrayList<String>();
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {}

    public void reset() {}

    public void init(Model model) {}

    public String getQualifiedName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    //**************************************************************************
    // Permission verification
    //**************************************************************************

    public boolean isAllowed(List<String> groups) {
        if (CollectionUtils.containsAny(deny, groups)) {
            return false;
        }

        //noinspection SimplifiableIfStatement
        if (allow.isEmpty()) {
            return true;
        }

        return CollectionUtils.containsAny(allow, groups);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlElementWrapper(name="allow")
    @XmlElement(name = "group", type = java.lang.String.class)
    public List<String> getAllow() {
        return allow;
    }

    @XmlElementWrapper(name="deny")
    @XmlElement(name = "group", type = java.lang.String.class)
    public List<String> getDeny() {
        return deny;
    }
}
