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

package com.manydesigns.portofino.pageactions.calendar;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.i18n.MultipleTextProvider;
import com.manydesigns.portofino.i18n.ResourceBundleManager;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.calendar.configuration.CalendarConfiguration;
import com.manydesigns.portofino.pageactions.custom.CustomAction;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ConfigurationClass(CalendarConfiguration.class)
public class CalendarAction extends CustomAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    //**************************************************************************
    // Variables
    //**************************************************************************

    protected MonthView monthView;
    protected AgendaView agendaView;
    protected DateTime referenceDateTime = new DateTime(DateTimeZone.UTC);
    protected final List<Calendar> calendars = new ArrayList<Calendar>();
    protected final List<Event> events = new ArrayList<Event>();

    protected String calendarViewType;

    //**************************************************************************
    // Support objects
    //**************************************************************************

    protected Form configurationForm;
    protected ResourceBundleManager resourceBundleManager;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(CalendarAction.class);

    //**************************************************************************
    // Setup and configuration
    //**************************************************************************

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        this.pageInstance = pageInstance;
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        if(pageInstance.getConfiguration() == null) {
            pageInstance.setConfiguration(new CalendarConfiguration());
        }
        return null;
    }

    @Override
    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/calendar/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution updateConfiguration() {
        prepareConfigurationForms();
        readPageConfigurationFromRequest();
        configurationForm.readFromRequest(context.getRequest());
        boolean valid = validatePageConfiguration();
        valid = valid && configurationForm.validate();
        if(valid) {
            updatePageConfiguration();
            configurationForm.writeToObject(pageInstance.getConfiguration());
            saveConfiguration(pageInstance.getConfiguration());
            SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
            return cancel();
        } else {
            SessionMessages.addErrorMessage(getMessage("commons.configuration.notUpdated"));
            return new ForwardResolution("/layouts/calendar/configure.jsp");
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        configurationForm = new FormBuilder(CalendarConfiguration.class).build();
        configurationForm.readFromObject(pageInstance.getConfiguration());
    }

    //**************************************************************************
    // Handlers
    //**************************************************************************

    @DefaultHandler
    @RequiresPermissions(level = AccessLevel.VIEW)
    public Resolution execute() {
        if("agenda".equals(calendarViewType)) {
            agendaView = new AgendaView(referenceDateTime);
            int maxEvents = getConfiguration().getEstimateEventsPerPageInAgendaView();
            loadObjects(agendaView.getFirstDay().toDateTime(), maxEvents);
            int added = 0;
            for(Event event : events) {
                added += agendaView.addEvent(event);
                if(added >= maxEvents) {
                    break;
                }
            }
            agendaView.sortEvents();
        } else {
            calendarViewType = "month";
            monthView = new MonthView(referenceDateTime);
            loadObjects(monthView.getMonthViewInterval());
            for(Event event : events) {
                monthView.addEvent(event);
            }
            monthView.sortEvents();
        }
        if (isEmbedded()) {
            return new ForwardResolution("/layouts/calendar/calendar.jsp");
        } else {
            return forwardToPortletPage("/layouts/calendar/calendar.jsp");
        }
    }

    public Resolution nextMonth() {
        referenceDateTime = referenceDateTime.plusMonths(1).withDayOfMonth(1).withTime(0, 0, 0, 0);
        return execute();
    }

    public Resolution prevMonth() {
        referenceDateTime = referenceDateTime.minusMonths(1).withDayOfMonth(1).withTime(0, 0, 0, 0);
        return execute();
    }

    public Resolution nextDay() {
        referenceDateTime = referenceDateTime.plusDays(1);
        return execute();
    }

    public Resolution prevDay() {
        referenceDateTime = referenceDateTime.minusDays(1);
        return execute();
    }

    public Resolution today() {
        referenceDateTime = new DateTime(DateTimeZone.UTC);
        return execute();
    }

    //--------------------------------------------------------------------------
    // Data provider
    //--------------------------------------------------------------------------

    public void loadObjects(Interval interval) {}

    public void loadObjects(DateTime instant, int maxEvents) {}

    public MonthView getMonthView() {
        return monthView;
    }

    public AgendaView getAgendaView() {
        return agendaView;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public long getReferenceDateTimeLong() {
        return referenceDateTime.getMillis();
    }

    public void setReferenceDateTimeLong(long millis) {
        this.referenceDateTime = new DateTime(millis, DateTimeZone.UTC);
    }

    public List<Calendar> getCalendars() {
        return calendars;
    }

    public List<Event> getEvents() {
        return events;
    }

    public String getCalendarViewType() {
        return calendarViewType;
    }

    public void setCalendarViewType(String calendarViewType) {
        this.calendarViewType = calendarViewType;
    }

    public DateTime getReferenceDateTime() {
        return referenceDateTime;
    }

    public Form getConfigurationForm() {
        return configurationForm;
    }

    public CalendarConfiguration getConfiguration() {
        return (CalendarConfiguration) pageInstance.getConfiguration();
    }
}
