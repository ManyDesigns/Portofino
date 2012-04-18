/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.pageactions.timesheet.model;

import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public enum DayStatus {
        OPEN, LOCKED
    }

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final LocalDate referenceDate;

    final LocalDate firstDate;
    final LocalDate lastDate;

    protected final Day[]          days;
    protected final List<Activity> activities;

    protected String personId;
    protected String personName;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(WeekEntryModel.class);

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public WeekEntryModel(LocalDate referenceDate) {
        logger.debug("Initializing week entry model");
        this.referenceDate = referenceDate;

        firstDate = referenceDate.withDayOfWeek(DateTimeConstants.MONDAY);
        lastDate = firstDate.plusWeeks(1).minusDays(1);

        logger.debug("Initializing weeks");
        days = new Day[7];
        LocalDate dayStart = firstDate;
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

    public Day findDayByDate(LocalDate date) {
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

    public void addEntry(LocalDate date, Activity activity, Entry entry) {
        Day day = findDayByDate(date);
        if (day == null) {
            logger.warn("Date not in range: {}", date);
        } else {
            logger.debug("Adding entry to day");
            day.addEntry(activity, entry);
        }
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------

    public static class Entry {

        int    minutes;
        String note;

        public Entry() {
        }

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
        final LocalDate date;
        Integer   standardWorkingMinutes;
        DayStatus status;
        boolean   nonWorking;
        boolean   today;

        final Map<Activity, Entry> entries;

        public Day(LocalDate date) {
            this.date = date;
            entries = new HashMap<Activity, Entry>();
        }

        public LocalDate getDate() {
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
            if (activity == null) {
                logger.warn("Null activity");
                return;
            }
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
