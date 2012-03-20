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

package com.manydesigns.portofino.pageactions.timesheet.util;

import com.manydesigns.portofino.pageactions.timesheet.model.Activity;
import com.manydesigns.portofino.pageactions.timesheet.model.Person;
import com.manydesigns.portofino.pageactions.timesheet.model.WeekEntryModel;
import org.jetbrains.annotations.Nullable;
import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PersonDay {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    final Person    person;
    final LocalDate date;
    Integer standardWorkingMinutes;
    boolean locked;

    final Map<Activity, WeekEntryModel.Entry> entries;

    public PersonDay(Person person, LocalDate date) {
        this(person, date, null, false);
    }

    public PersonDay(Person person,
                     LocalDate date,
                     @Nullable Integer standardWorkingMinutes,
                     boolean locked) {
        this.person = person;
        this.date = date;
        this.standardWorkingMinutes = standardWorkingMinutes;
        this.locked = locked;
        entries = new HashMap<Activity, WeekEntryModel.Entry>();
    }

    public Person getPerson() {
        return person;
    }

    public LocalDate getDate() {
        return date;
    }

    public Map<Activity, WeekEntryModel.Entry> getEntries() {
        return entries;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Integer getStandardWorkingMinutes() {
        return standardWorkingMinutes;
    }

    public void setStandardWorkingMinutes(Integer standardWorkingMinutes) {
        this.standardWorkingMinutes = standardWorkingMinutes;
    }

    public WeekEntryModel.Entry addEntry(Activity activity, int minutes, @Nullable String note) {
        WeekEntryModel.Entry entry = new WeekEntryModel.Entry(minutes, note);
        entries.put(activity, entry);
        return entry;
    }
}
