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

package com.manydesigns.portofino.pageactions.calendar.configuration;

import com.manydesigns.elements.annotations.FieldSet;
import com.manydesigns.elements.annotations.LabelI18N;
import com.manydesigns.elements.annotations.MinIntValue;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.PageActionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class CalendarConfiguration implements PageActionConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

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

    public void init(Application application) {}

    //**************************************************************************
    // Getters/setters
    //**************************************************************************


    @XmlAttribute
    @Required
    @FieldSet("calendar.configuration.monthView")
    @LabelI18N("calendar.maxEventsPerCellInMonthView")
    @MinIntValue(1)
    public int getMaxEventsPerCellInMonthView() {
        return maxEventsPerCellInMonthView;
    }

    public void setMaxEventsPerCellInMonthView(int maxEventsPerCellInMonthView) {
        this.maxEventsPerCellInMonthView = maxEventsPerCellInMonthView;
    }

    @XmlAttribute
    @Required
    @FieldSet("calendar.configuration.agendaView")
    @LabelI18N("calendar.estimateEventsPerPageInAgendaView")
    @MinIntValue(1)
    public int getEstimateEventsPerPageInAgendaView() {
        return estimateEventsPerPageInAgendaView;
    }

    public void setEstimateEventsPerPageInAgendaView(int estimateEventsPerPageInAgendaView) {
        this.estimateEventsPerPageInAgendaView = estimateEventsPerPageInAgendaView;
    }
}
