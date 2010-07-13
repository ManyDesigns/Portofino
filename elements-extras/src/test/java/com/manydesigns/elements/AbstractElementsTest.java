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

package com.manydesigns.elements;

import com.manydesigns.elements.fields.helpers.registry.DefaultRegistryBuilder;
import com.manydesigns.elements.fields.helpers.registry.FieldHelperRegistry;
import com.manydesigns.elements.fields.helpers.registry.RegistryBuilder;
import com.manydesigns.elements.reflection.FieldAccessor;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XmlBuffer;
import com.manydesigns.elements.text.BasicTextProvider;
import junit.framework.TestCase;

import java.util.Locale;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class AbstractElementsTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2009, ManyDesigns srl";

    public RegistryBuilder registryBuilder;
    public FieldHelperRegistry fieldHelperRegistry;
    public DummyHttpServletRequest req;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        XmlBuffer.checkWellFormed = true;

        req = new DummyHttpServletRequest();
        req.setContextPath("");

        ElementsThreadLocals.setHttpServletRequest(req);

        ElementsThreadLocals.setTextProvider(
                new BasicTextProvider(Locale.ENGLISH));

        registryBuilder = new DefaultRegistryBuilder();
        fieldHelperRegistry = registryBuilder.build();
        ElementsThreadLocals.setFieldHelper(fieldHelperRegistry);
    }

    public FieldAccessor createPropertyAccessor(Class clazz,
                                                String fieldName)
            throws NoSuchFieldException {
        java.lang.reflect.Field field = clazz.getField(fieldName);
        return new FieldAccessor(field);
    }


    public String elementToString(Element element) {
        XhtmlBuffer xb = new XhtmlBuffer();
        element.toXhtml(xb);
        return xb.toString();
    }
}
