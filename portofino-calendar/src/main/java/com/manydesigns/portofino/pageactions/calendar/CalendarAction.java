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

package com.manydesigns.portofino.pageactions.calendar;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.calendar.configuration.CalendarConfiguration;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ConfigurationClass(CalendarConfiguration.class)
@PageActionName("Calendar")
@ScriptTemplate("script_template.groovy")
public class CalendarAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

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

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(CalendarAction.class);

    //**************************************************************************
    // Setup and configuration
    //**************************************************************************

    public Resolution preparePage() {
        if(pageInstance.getConfiguration() == null) {
            pageInstance.setConfiguration(new CalendarConfiguration());
        }
        return null;
    }

    @Button(list = "pageHeaderButtons", titleKey = "configure", order = 1, icon = Button.ICON_WRENCH)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/m/calendar/configure.jsp");
    }

    @Button(list = "configuration", key = "update.configuration", type = Button.TYPE_PRIMARY)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
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
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("configuration.updated.successfully"));
            return cancel();
        } else {
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("the.configuration.could.not.be.saved"));
            return new ForwardResolution("/m/calendar/configure.jsp");
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
            return agendaView();
        } else {
            return monthView();
        }
    }

    public Resolution monthView() {
        calendarViewType = "month";
        monthView = new MonthView(referenceDateTime);
        loadObjects(monthView.getMonthViewInterval());
        for(Event event : events) {
            monthView.addEvent(event);
        }
        monthView.sortEvents();
        return new ForwardResolution("/m/calendar/calendar.jsp");
    }

    public Resolution agendaView() {
        calendarViewType = "agenda";
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
        return new ForwardResolution("/m/calendar/calendar.jsp");
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
