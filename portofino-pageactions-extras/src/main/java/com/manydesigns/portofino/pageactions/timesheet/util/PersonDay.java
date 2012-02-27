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
            "Copyright (c) 2005-2011, ManyDesigns srl";

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
