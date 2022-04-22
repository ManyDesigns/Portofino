/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.model;

import org.apache.commons.configuration2.Configuration;
import org.eclipse.emf.ecore.EModelElement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * A property of an {@link Annotation annotation} on a model object.
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@XmlAccessorType(XmlAccessType.NONE)
public class AnnotationProperty implements ModelObject {

    protected Annotation owner;
    protected String name;
    protected String value;

    public AnnotationProperty() {}

    public AnnotationProperty(Annotation annotation, String name, String value) {
        setName(name);
        setValue(value);
        setParent(annotation);
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute
    public String getValue() {
        return owner != null ? owner.eAnnotation.getDetails().get(name) : value;
    }

    public void setValue(String value) {
        if(owner != null) {
            owner.eAnnotation.getDetails().put(name, value);
        } else {
            this.value = value;
        }
    }

    @Override
    public void setParent(Object parent) {
        Annotation newOwner = (Annotation) parent;
        String value = getValue();
        if(owner != null) {
            owner.eAnnotation.getDetails().removeKey(getName());
        }
        this.owner = newOwner;
        setValue(value);
    }

    @Override
    public void reset() {}

    @Override
    public void init(Object context, Configuration configuration) {}

    @Override
    public void link(Object context, Configuration configuration) {}

    @Override
    public void visitChildren(ModelObjectVisitor visitor) {}

    @Override
    public EModelElement getModelElement() {
        return null;
    }
}
