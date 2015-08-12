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

package com.manydesigns.portofino.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@XmlAccessorType(value = XmlAccessType.NONE)
@XmlType(propOrder = {"order","showInNavigation","name", "container"})
public class ChildPage {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String name;
    protected String container;
    protected String order;
    protected boolean showInNavigation;

    //**************************************************************************
    // Actual fields
    //**************************************************************************

    protected int actualOrder;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(ChildPage.class);

    //**************************************************************************
    // Constructors & initialization
    //**************************************************************************

    public ChildPage() {
    }

    public void init() {
        actualOrder = 0;
        if(order != null) {
            try {
                actualOrder = Integer.parseInt(order);
            } catch (NumberFormatException e) {
                logger.warn("Cannot parse value of 'order': " + order, e);
            }
        }
    }

    //**************************************************************************
    // Getters/Setters
    //**************************************************************************

    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(required = false)
    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    @XmlAttribute(required = false)
    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    @XmlAttribute(required = false)
    public boolean isShowInNavigation() {
        return showInNavigation;
    }

    public void setShowInNavigation(boolean showInNavigation) {
        this.showInNavigation = showInNavigation;
    }

    public int getActualOrder() {
        return actualOrder;
    }
}
