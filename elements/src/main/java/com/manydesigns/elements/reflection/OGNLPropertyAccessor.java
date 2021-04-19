/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.reflection;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.ognl.OgnlUtils;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import java.lang.annotation.Annotation;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class OGNLPropertyAccessor extends AbstractAnnotatedAccessor implements PropertyAccessor {
    
    protected final String name;
    protected final Class type;
    protected final String expression;
    protected final Object parsedExpression;
    protected OgnlContext ognlContext;
    protected int modifiers;
    
    public OGNLPropertyAccessor(String name, Class type, String expression, OgnlContext ognlContext) throws OgnlException {
        this.name = name;
        this.type = type;
        this.expression = expression;
        this.parsedExpression = Ognl.parseExpression(expression);
        this.ognlContext = ognlContext;
    }
    
    public OGNLPropertyAccessor(String name, Class type, String expression) throws OgnlException {
        this(name, type, expression, null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class getType() {
        return type;
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public OgnlContext getOgnlContext() {
        return ognlContext;
    }

    public void setOgnlContext(OgnlContext ognlContext) {
        this.ognlContext = ognlContext;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public Object get(Object obj) {
        try {
            return Ognl.getValue(parsedExpression, getActualOgnlContext(), obj, type);
        } catch (OgnlException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(Object obj, Object value) {
        try {
            Object convertedValue = OgnlUtils.convertValue(value, type);
            Ognl.setValue(parsedExpression, getActualOgnlContext(), obj, convertedValue);
        } catch (OgnlException e) {
            throw new RuntimeException(e);
        }
    }

    public OgnlContext getActualOgnlContext() {
        return ognlContext != null ? ognlContext : ElementsThreadLocals.getOgnlContext();
    }

    public OGNLPropertyAccessor configureAnnotation(Annotation annotation) {
        annotations.put(annotation.annotationType(), annotation);
        return this;
    }
    
}
