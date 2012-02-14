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

package com.manydesigns.portofino.pageactions.timesheet.model;

import org.jetbrains.annotations.Nullable;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class WeekEntryModel {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public enum DayStatus {
        OPEN, LOCKED
    }

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final DateMidnight referenceDateMidnight;
    final int referenceYear;
    final int referenceMonth;

    final DateMidnight weekStart;
    final DateMidnight weekEnd;
    final Interval weekInterval;

    protected final Day[] days;
    protected final List<Activity> activities;

    protected Person person;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(WeekEntryModel.class);

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public WeekEntryModel(DateMidnight referenceDateMidnight) {
        logger.debug("Initializing week entry model");
        this.referenceDateMidnight = referenceDateMidnight;
        referenceYear = referenceDateMidnight.getYear();
        referenceMonth = referenceDateMidnight.getMonthOfYear();

        weekStart = referenceDateMidnight.withDayOfWeek(DateTimeConstants.MONDAY);
        weekEnd = weekStart.plusWeeks(1);
        weekInterval = new Interval(weekStart, weekEnd);

        logger.debug("Initializing weeks");
        days = new Day[7];
        DateMidnight dayStart = weekStart;
        for (int i = 0; i < days.length; i++) {
            days[i] = new Day(dayStart);

            dayStart = dayStart.plusDays(1);
        }

        activities = new ArrayList<Activity>();
    }

    //--------------------------------------------------------------------------
    // Accessors
    //--------------------------------------------------------------------------

    public List<Activity> getActivities() {
        return activities;
    }

    public Day getDay(int index) {
        return days[index];
    }

    public Day findDayByDate(DateMidnight date) {
        for (Day day : days) {
            if (day.getDate().equals(date)) {
                return day;
            }
        }
        return null;
    }

    public Activity findActivityById(String id) {
        for (Activity activity : activities) {
            if (activity.getId().equals(id)) {
                return activity;
            }
        }
        return null;
    }

    public void addEntry(DateMidnight date, Activity activity, Entry entry) {
        Day day = findDayByDate(date);
        if (day == null) {
            logger.warn("Date not in range: {}", date);
        } else {
            logger.debug("Adding entry to day");
            day.addEntry(activity, entry);
        }
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------

    public static class Entry {

        int minutes;
        String note;

        public Entry(int minutes, String note) {
            this.minutes = minutes;
            this.note = note;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public int getMinutes() {
            return minutes;
        }

        public void setMinutes(int minutes) {
            this.minutes = minutes;
        }
    }


    public class Day {
        final DateMidnight date;
        Integer standardWorkingMinutes;
        DayStatus status;
        boolean nonWorking;
        boolean today;

        final Map<Activity, Entry> entries;

        public Day(DateMidnight date) {
            this.date = date;
            entries = new HashMap<Activity, Entry>();
        }

        public DateMidnight getDate() {
            return date;
        }

        public Integer getStandardWorkingMinutes() {
            return standardWorkingMinutes;
        }

        public void setStandardWorkingMinutes(Integer standardWorkingMinutes) {
            this.standardWorkingMinutes = standardWorkingMinutes;
        }

        public boolean isNonWorking() {
            return nonWorking;
        }

        public void setNonWorking(boolean nonWorking) {
            this.nonWorking = nonWorking;
        }

        public DayStatus getStatus() {
            return status;
        }

        public void setStatus(@Nullable DayStatus status) {
            this.status = status;
        }

        public boolean isToday() {
            return today;
        }

        public void setToday(boolean today) {
            this.today = today;
        }

        public void addEntry(Activity activity, int minutes, String note) {
            Entry entry = new Entry(minutes, note);
            addEntry(activity, entry);
        }

        public void addEntry(Activity activity, Entry entry) {
            if (!activities.contains(activity)) {
                logger.warn("Activity {} not in week entry model", activity.getId());
                return;
            }
            entries.put(activity, entry);
        }

        public Entry findEntryByActivity(Activity activity) {
            return entries.get(activity);
        }
    }

}
