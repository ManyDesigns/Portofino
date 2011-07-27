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

package com.manydesigns.portofino.model.site.crud;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.xml.Identifier;

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
public class Button implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String DEFAULT_BUTTON_METHOD = "button";
    public final static String DEFAULT_SCRIPT_LANGUAGE = "ognl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Crud crud;

    protected String name;
    protected String label;
    protected String method;
    protected String guard;
    protected String script;
    protected String scriptLanguage;

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String actualMethod;
    protected String actualScriptLanguage;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Button() {}

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        crud = (Crud) parent;
    }

    public void reset() {
        actualMethod = null;
        actualScriptLanguage = null;
    }

    public void init(Model model) {
        assert crud != null;
        assert name != null;
        assert label != null;

        actualMethod = (method == null)
                ? DEFAULT_BUTTON_METHOD
                : method;

        actualScriptLanguage = (scriptLanguage == null)
                ? DEFAULT_SCRIPT_LANGUAGE
                : scriptLanguage;
    }

    public String getQualifiedName() {
        return String.format("%s*%s", crud.getQualifiedName(), name);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public Crud getCrud() {
        return crud;
    }

    public void setCrud(Crud crud) {
        this.crud = crud;
    }

    @Identifier
    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(required = true)
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @XmlAttribute(required = false)
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @XmlAttribute(required = false)
    public String getGuard() {
        return guard;
    }

    public void setGuard(String guard) {
        this.guard = guard;
    }

    @XmlAttribute(required = false)
    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @XmlAttribute(required = false)
    public String getScriptLanguage() {
        return scriptLanguage;
    }

    public void setScriptLanguage(String scriptLanguage) {
        this.scriptLanguage = scriptLanguage;
    }

    public String getActualMethod() {
        return actualMethod;
    }

    public void setActualMethod(String actualMethod) {
        this.actualMethod = actualMethod;
    }

    public String getActualScriptLanguage() {
        return actualScriptLanguage;
    }

    public void setActualScriptLanguage(String actualScriptLanguage) {
        this.actualScriptLanguage = actualScriptLanguage;
    }
}
