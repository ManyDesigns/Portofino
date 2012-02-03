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

package com.manydesigns.portofino.pageactions.timesheet;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pageactions.calendar.MonthView;
import com.manydesigns.portofino.pageactions.custom.CustomAction;
import com.manydesigns.portofino.pageactions.timesheet.model.*;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.stripes.NoCacheStreamingResolution;
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions;
import net.sourceforge.stripes.action.*;
import org.apache.commons.io.IOUtils;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
public class TimesheetAction extends CustomAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    //**************************************************************************
    // Variables
    //**************************************************************************

    protected Form form;

    protected String personId;
    protected int weeksAgo;
    protected DateMidnight today;
    protected DateMidnight monday;

    protected final List<Person> availablePersons = new ArrayList<Person>();
    protected final List<Activity> weekActivities = new ArrayList<Activity>();
    protected final Map<DateMidnight, PersonDay> personDays =
            new HashMap<DateMidnight, PersonDay>();
    protected final Set<DateMidnight> nonWorkingDays =
            new HashSet<DateMidnight>();

    protected MonthView monthView;

    protected Integer day;
    protected Integer month;
    protected Integer year;
    protected boolean nonWorking;

    //**************************************************************************
    // Injections
    //**************************************************************************

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(TimesheetAction.class);

    //--------------------------------------------------------------------------
    // Scripting
    //--------------------------------------------------------------------------

    public static final String SCRIPT_TEMPLATE;

    static {
        String scriptTemplate;
        try {
            scriptTemplate = IOUtils.toString(
                    TimesheetAction.class.getResourceAsStream("script_template.txt"));
        } catch (Exception e) {
            throw new Error("Can't load script template", e);
        }
        SCRIPT_TEMPLATE = scriptTemplate;
    }

    @Override
    public String getScriptTemplate() {
        return SCRIPT_TEMPLATE;
    }

    //**************************************************************************
    // Setup
    //**************************************************************************

    public Class<?> getConfigurationClass() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        this.pageInstance = pageInstance;
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        return null;
    }

    //**************************************************************************
    // Default view
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        loadExecuteModel();
        TimesheetSelection timesheetSelection = new TimesheetSelection();
        FormBuilder formBuilder =
                new FormBuilder(TimesheetSelection.class);

        // selection provider persone
        DefaultSelectionProvider personSelectionProvider =
                new DefaultSelectionProvider("person");
        for (Person person : availablePersons) {
            String name;
            if (person.isMe()) {
                name = "Io";
                timesheetSelection.personId = person.getId();
            } else {
                name = person.getLongName();
            }
            personSelectionProvider.appendRow(person.getId(), name, true);
        }
        formBuilder.configSelectionProvider(personSelectionProvider, "personId");

        // selection provider persone
        DefaultSelectionProvider weeksAgoSelectionProvider =
                new DefaultSelectionProvider("weeksAgo");

        today = new DateMidnight(dtz);
        monday = today.withDayOfWeek(DateTimeConstants.MONDAY);
        DateTimeFormatter formatter = DateTimeFormat.shortDate().withLocale(Locale.ITALY);
        for (int i = 0; i < 10; i++) {
            DateMidnight sunday = monday.plusDays(6);
            String label = formatter.print(monday) + " - " + formatter.print(sunday);
            weeksAgoSelectionProvider.appendRow(i, label, true);
            monday = monday.minusWeeks(1);
        }
        formBuilder.configSelectionProvider(weeksAgoSelectionProvider, "weeksAgo");

        form = formBuilder.build();
        form.readFromObject(timesheetSelection);
        if (isEmbedded()) {
            return new ForwardResolution("/layouts/timesheet/index.jsp");
        } else {
            return forwardToPortletPage("/layouts/timesheet/index.jsp");
        }
    }

    //**************************************************************************
    // Week data entry
    //**************************************************************************

    @Button(list = "timesheet-selection", key = "Go to timesheet", order = 1)
    public Resolution weekEntry() {
        assert weeksAgo >= 0;
        today = new DateMidnight(dtz);
        monday = today.withDayOfWeek(DateTimeConstants.MONDAY).minusWeeks(weeksAgo);
        loadNonWorkingDays();
        loadWeekEntryModel();
        return new ForwardResolution("/layouts/timesheet/week-entry.jsp");
    }

    //**************************************************************************
    // Non working days view
    //**************************************************************************

    @Button(list = "timesheet-admin", key = "Manage non-working days", order = 1)
    public Resolution nonWorkingDays() {
        today = new DateMidnight(dtz);
        year = today.getYear();
        month = today.getMonthOfYear();
        loadNonWorkingDays();
        monthView = new MonthView(new DateTime(dtz));
        return new ForwardResolution("/layouts/timesheet/non-working-days.jsp");
    }

    @Button(list = "timesheet-week-entry", key = "commons.save", order = 1)
    public Resolution saveWeekEntry() {
        SessionMessages.addInfoMessage("Timesheet salvato con successo");
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    public Resolution configureNonWorkingDay() throws JSONException {
        logger.info("Configuring non working day. Year/month/day: {}/{}/{}. Non-working: {}",
                new Object[] {year, month, day, nonWorking});
        today = new DateMidnight(year, month, day, dtz);
        saveNonWorkingDay(today, nonWorking);
        JSONStringer js = new JSONStringer();
        js.object()
                .key("result")
                .value("ok")
                .endObject();
        String jsonText = js.toString();
        return new NoCacheStreamingResolution("application/json", jsonText);
    }

    //**************************************************************************
    // Other action handlers
    //**************************************************************************

    @Override
    @Buttons({
            @Button(list = "timesheet-week-entry", key = "commons.cancel", order = 99),
            @Button(list = "timesheet-non-working-days", key = "commons.ok", order = 99)
    })
    @RequiresPermissions(level = AccessLevel.VIEW)
    public Resolution cancel() {
        return super.cancel();
    }

    //--------------------------------------------------------------------------
    // Data provider
    //--------------------------------------------------------------------------

    static DateTimeZone dtz = DateTimeZone.UTC;

    static DateMidnight dec26 = new DateMidnight(
            2011, DateTimeConstants.DECEMBER, 26,
            dtz);
    static DateMidnight dec27 = dec26.plusDays(1);
    static DateMidnight dec28 = dec27.plusDays(1);
    static DateMidnight dec29 = dec28.plusDays(1);
    static DateMidnight dec30 = dec29.plusDays(1);
    static DateMidnight dec31 = dec30.plusDays(1);
    static DateMidnight jan1 = dec31.plusDays(1);
    static DateMidnight jan2 = jan1.plusDays(1);
    static DateMidnight jan3 = jan2.plusDays(1);
    static DateMidnight jan4 = jan3.plusDays(1);
    static DateMidnight jan5 = jan4.plusDays(1);
    static DateMidnight jan6 = jan5.plusDays(1);
    static DateMidnight jan7 = jan6.plusDays(1);
    static DateMidnight jan8 = jan7.plusDays(1);
    static DateMidnight jan9 = jan8.plusDays(1);
    static DateMidnight jan10 = jan9.plusDays(1);
    static DateMidnight jan11 = jan10.plusDays(1);
    static DateMidnight jan12 = jan11.plusDays(1);
    static DateMidnight jan13 = jan12.plusDays(1);
    static DateMidnight jan14 = jan13.plusDays(1);
    static DateMidnight jan15 = jan14.plusDays(1);
    static DateMidnight jan16 = jan15.plusDays(1);
    static DateMidnight jan17 = jan16.plusDays(1);
    static DateMidnight jan18 = jan17.plusDays(1);
    static DateMidnight jan19 = jan18.plusDays(1);
    static DateMidnight jan20 = jan19.plusDays(1);
    static DateMidnight jan21 = jan20.plusDays(1);
    static DateMidnight jan22 = jan21.plusDays(1);
    static DateMidnight jan23 = jan22.plusDays(1);
    static DateMidnight jan24 = jan23.plusDays(1);
    static DateMidnight jan25 = jan24.plusDays(1);
    static DateMidnight jan26 = jan25.plusDays(1);
    static DateMidnight jan27 = jan26.plusDays(1);
    static DateMidnight jan28 = jan27.plusDays(1);
    static DateMidnight jan29 = jan28.plusDays(1);
    static DateMidnight jan30 = jan29.plusDays(1);
    static DateMidnight jan31 = jan30.plusDays(1);
    static DateMidnight feb1 = jan31.plusDays(1);
    static DateMidnight feb2 = feb1.plusDays(1);
    static DateMidnight feb3 = feb2.plusDays(1);
    static DateMidnight feb4 = feb3.plusDays(1);
    static DateMidnight feb5 = feb4.plusDays(1);
    static DateMidnight feb6 = feb5.plusDays(1);

    static ActivityType at0 = new ActivityType("at0", "fatturabile", ActivityMetaType.BILLABLE);
    static ActivityType at1 = new ActivityType("at1", "non fatturabile", ActivityMetaType.NON_BILLABLE);
    static ActivityType at2 = new ActivityType("at2", "assenza", ActivityMetaType.LEAVE);

    static Activity ac1 = new Activity("ac1", "#0100", "Acme System", "Analisi", "Analisi dei requisiti", at0, null, null, null);
    static Activity ac2 = new Activity("ac2", "#0100", "Acme System", "Progettazione", null, at0, null, null, null);
    static Activity ac3 = new Activity("ac3", "#0100", "Acme System", "Sviluppo", null, at0, null, null, null);

    static Activity ac4 = new Activity("ac4", "#0101", "Bunga website", "Supporto e gestione", null, at0, null, null, null);

    static Activity ac5 = new Activity("ac5", "#0001", "Processi interni", "Marketing", null, at1, null, null, "http://www.manydesigns.com/");
    static Activity ac6 = new Activity("ac6", "#0001", "Processi interni", "Formazione", null, at1, null, null, null);

    static Activity ac7 = new Activity("ac7", "#0002", "Assenze", "Malattia", null, at2, null, null, null);
    static Activity ac8 = new Activity("ac8", "#0002", "Assenze", "Ferie", null, at2, null, null, null);
    static Activity ac9 = new Activity("ac9", "#0002", "Assenze", "Permesso", null, at2, null, null, null);

    static Person paolo = new Person("paolo", "Paolo Predonzani", null, null, true);
    static Person angelo = new Person("angelo", "Angelo Lupo", null, null, false);

    static PersonDay paoloJan30 = new PersonDay(paolo, jan30, null, true);
    static PersonDay paoloJan31 = new PersonDay(paolo, jan31, null, false);

    static Entry entry1 = new Entry(ac1, new Period(1, 30, 0, 0), "Intervista con Pippo");
    static Entry entry2 = new Entry(ac5, new Period(3, 0, 0, 0), null);
    static Entry entry3 = new Entry(ac1, new Period(4, 0, 0, 0), "Formalizzazione requisiti");
    static Entry entry4 = new Entry(ac3, new Period(4, 0, 0, 0), null);
    static Entry entry5 = new Entry(ac5, new Period(4, 0, 0, 0), null);
    static Entry entry6 = new Entry(ac7, new Period(4, 0, 0, 0), null);

    static Map<DateMidnight, PersonDay> personDayDb
            = new HashMap<DateMidnight, PersonDay>();

    static Set<DateMidnight> nonWorkingDaysDb
            = new HashSet<DateMidnight>();

    static {
        personDayDb.put(paoloJan30.getDate(), paoloJan30);
        personDayDb.put(paoloJan31.getDate(), paoloJan31);

        paoloJan30.getEntries().add(entry1);
        paoloJan30.getEntries().add(entry2);
        paoloJan31.getEntries().add(entry3);
        paoloJan31.getEntries().add(entry4);
        paoloJan31.getEntries().add(entry5);
        paoloJan31.getEntries().add(entry6);

        nonWorkingDaysDb.add(jan28);
        nonWorkingDaysDb.add(jan29);
        nonWorkingDaysDb.add(feb4);
        nonWorkingDaysDb.add(feb5);
    }

    public void loadExecuteModel() {
        availablePersons.add(paolo);
        availablePersons.add(angelo);
    }

    public void loadWeekEntryModel() {
        weekActivities.add(ac1);
        weekActivities.add(ac2);
        weekActivities.add(ac3);
        weekActivities.add(ac4);
        weekActivities.add(ac5);
        weekActivities.add(ac6);
        weekActivities.add(ac7);
        weekActivities.add(ac8);
        weekActivities.add(ac9);

        for (Map.Entry<DateMidnight, PersonDay> current : personDayDb.entrySet()) {
            DateMidnight dateMidnight = current.getKey();
            PersonDay personDay = current.getValue();
            if (nonWorkingDays.contains(dateMidnight)) {
                personDay.setStandardWorkingMinutes(0);
            } else {
                personDay.setStandardWorkingMinutes(8);
            }
            personDays.put(dateMidnight, personDay);
        }
    }

    public void loadNonWorkingDays() {
        nonWorkingDays.addAll(nonWorkingDaysDb);
    }

    public void saveNonWorkingDay(DateMidnight date, boolean nonWorking) {
        if (nonWorking) {
            nonWorkingDaysDb.add(date);
        } else {
            nonWorkingDaysDb.remove(date);
        }
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------


    public Form getForm() {
        return form;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public int getWeeksAgo() {
        return weeksAgo;
    }

    public void setWeeksAgo(int weeksAgo) {
        this.weeksAgo = weeksAgo;
    }

    public DateMidnight getToday() {
        return today;
    }

    public DateMidnight getMonday() {
        return monday;
    }

    public List<Person> getAvailablePersons() {
        return availablePersons;
    }

    public List<Activity> getWeekActivities() {
        return weekActivities;
    }

    public Map<DateMidnight, PersonDay> getPersonDays() {
        return personDays;
    }

    public Set<DateMidnight> getNonWorkingDays() {
        return nonWorkingDays;
    }

    public MonthView getMonthView() {
        return monthView;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public boolean getNonWorking() {
        return nonWorking;
    }

    public void setNonWorking(boolean nonWorking) {
        this.nonWorking = nonWorking;
    }
}
