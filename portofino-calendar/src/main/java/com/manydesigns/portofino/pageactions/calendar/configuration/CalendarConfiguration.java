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

package com.manydesigns.portofino.pageactions.calendar.configuration;

import com.manydesigns.elements.annotations.FieldSet;
import com.manydesigns.elements.annotations.LabelI18N;
import com.manydesigns.elements.annotations.MinIntValue;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.dispatcher.PageActionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"maxEventsPerCellInMonthView","estimateEventsPerPageInAgendaView"})
public class CalendarConfiguration implements PageActionConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected int maxEventsPerCellInMonthView = 3;
    protected int estimateEventsPerPageInAgendaView = 10;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(CalendarConfiguration.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public CalendarConfiguration() {
        super();
    }

    //**************************************************************************
    // Configuration implementation
    //**************************************************************************

    public void init() {}

    //**************************************************************************
    // Getters/setters
    //**************************************************************************


    @XmlAttribute
    @Required
    @FieldSet("month.view")
    @LabelI18N("max.events.per.day")
    @MinIntValue(1)
    public int getMaxEventsPerCellInMonthView() {
        return maxEventsPerCellInMonthView;
    }

    public void setMaxEventsPerCellInMonthView(int maxEventsPerCellInMonthView) {
        this.maxEventsPerCellInMonthView = maxEventsPerCellInMonthView;
    }

    @XmlAttribute
    @Required
    @FieldSet("agenda.view")
    @LabelI18N("events.per.page")
    @MinIntValue(1)
    public int getEstimateEventsPerPageInAgendaView() {
        return estimateEventsPerPageInAgendaView;
    }

    public void setEstimateEventsPerPageInAgendaView(int estimateEventsPerPageInAgendaView) {
        this.estimateEventsPerPageInAgendaView = estimateEventsPerPageInAgendaView;
    }
}
