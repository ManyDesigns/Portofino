/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements;

import com.manydesigns.elements.xml.XhtmlBuffer;

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public interface Field extends Element {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    void valueToXhtml(XhtmlBuffer xb);
    void labelToXhtml(XhtmlBuffer xb);
    void helpToXhtml(XhtmlBuffer xb);
    void errorsToXhtml(XhtmlBuffer xb);

    public String  getId();
    void setId(String id);

    String getLabel();
    void setLabel(String label);

    String getInputName();
    void setInputName(String inputName);

    boolean isRequired();
    void setRequired(boolean required);

    boolean isForceNewRow();
    void setForceNewRow(boolean forceNewRow);

    int getColSpan();
    void setColSpan(int colSpan);

    String getHelp();
    void setHelp(String help);

    List<String> getErrors();


    public String getHref();
    public void setHref(String href);

    public String getAlt();
    public void setAlt(String alt);

}
