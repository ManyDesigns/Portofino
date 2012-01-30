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

import org.joda.time.DateMidnight;

import java.math.BigDecimal;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Entry {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    final Person person;
    final Activity activity;
    final DateMidnight date;
    BigDecimal hours;
    String note;
    boolean locked;

    public Entry(Person person, Activity activity, DateMidnight date) {
        this.person = person;
        this.activity = activity;
        this.date = date;
    }

    public Entry(Person person, Activity activity, DateMidnight date, BigDecimal hours, String note, boolean locked) {
        this.person = person;
        this.activity = activity;
        this.date = date;
        this.hours = hours;
        this.note = note;
        this.locked = locked;
    }

    public Person getPerson() {
        return person;
    }

    public Activity getActivity() {
        return activity;
    }

    public DateMidnight getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
