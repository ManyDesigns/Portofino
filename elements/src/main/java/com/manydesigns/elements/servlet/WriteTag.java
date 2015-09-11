/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.servlet;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class WriteTag extends TagSupport {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public static final String APPLICATION_SCOPE = "APPLICATION";
    public static final String SESSION_SCOPE = "SESSION";
    public static final String REQUEST_SCOPE = "REQUEST";
    public static final String PAGE_SCOPE = "PAGE";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected String name;
    protected String property;
    protected String scope;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger = LoggerFactory.getLogger(WriteTag.class);

    //--------------------------------------------------------------------------
    // TabSupport override
    //--------------------------------------------------------------------------

    @Override
    public int doStartTag() {
        JspWriter out = pageContext.getOut();
        try {
            doTag(out);
        } catch (Throwable e) {
            throw new Error(e);
        }
        return SKIP_BODY;
    }

    private void doTag(JspWriter out) throws OgnlException {
        Integer scopeCode;
        if (scope == null) {
            scopeCode = null;
        } else if (APPLICATION_SCOPE.equals(scope)){
                scopeCode = PageContext.APPLICATION_SCOPE;
        } else if (SESSION_SCOPE.equals(scope)){
            scopeCode = PageContext.SESSION_SCOPE;
        } else if (REQUEST_SCOPE.equals(scope)){
            scopeCode = PageContext.REQUEST_SCOPE;
        } else if (PAGE_SCOPE.equals(scope)){
            scopeCode = PageContext.PAGE_SCOPE;
        } else {
            logger.warn("Unknown scope: {}", scope);
            return;
        }

        Object bean;
        if (scopeCode != null) {
            bean = pageContext.getAttribute(name, scopeCode);
        } else {
            bean = pageContext.findAttribute(name);
        }

        if (bean == null) {
            logger.warn("Bean {} not found in scope {}", name, scope);
            return;
        }

        if (property != null) {
            // use property as Ognl expression
            OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
            bean = OgnlUtils.getValueQuietly(property, ognlContext, bean);
        }

        if (bean instanceof XhtmlFragment) {
            XhtmlFragment xhtmlFragment = (XhtmlFragment) bean;
            XhtmlBuffer xb = new XhtmlBuffer(out);
            xhtmlFragment.toXhtml(xb);
        } else {
            logger.warn("Bean {} scope {} property {} not of type XhtmlFragment: {}",
                    new String[] {name, scope, property, bean != null ? bean.getClass().getName() : null});
        }
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
    
}
