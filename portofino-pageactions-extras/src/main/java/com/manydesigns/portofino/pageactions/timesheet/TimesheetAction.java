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
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pageactions.custom.CustomAction;
import com.manydesigns.portofino.pageactions.timesheet.model.Person;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions;
import net.sourceforge.stripes.action.*;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected List<Person> availablePersons;

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
    // Handlers
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        availablePersons = new ArrayList<Person>();
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

        DateTime today = new DateTime();
        DateTime monday = today.withDayOfWeek(DateTimeConstants.MONDAY);
        DateTimeFormatter formatter = DateTimeFormat.shortDate().withLocale(Locale.ITALY);
        for (int i = 0; i < 10; i++) {
            DateTime sunday = monday.plusDays(6);
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

    @Button(list = "timesheet-selection", key = "Go to timesheet", order = 1)
    public Resolution weekEntry() {
        return new ForwardResolution("/layouts/timesheet/week-entry.jsp");
    }

    //--------------------------------------------------------------------------
    // Data provider
    //--------------------------------------------------------------------------

    public void loadExecuteModel() {
        Person paolo = new Person("paolo", "Paolo Predonzani", null, null, true);
        availablePersons.add(paolo);
        Person angelo = new Person("angelo", "Angelo Lupo", null, null, false);
        availablePersons.add(angelo);
    }

    public void loadWeekEntryModel() {

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
}
