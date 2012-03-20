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

import org.joda.time.DateMidnight;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Person {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final String id;
    String longName;
    DateMidnight firstDay;
    DateMidnight lastDay;
    boolean me;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public Person(String id) {
        this.id = id;
    }

    public Person(String id, String longName, DateMidnight firstDay, DateMidnight lastDay, boolean me) {
        this.id = id;
        this.longName = longName;
        this.firstDay = firstDay;
        this.lastDay = lastDay;
        this.me = me;
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public DateMidnight getFirstDay() {
        return firstDay;
    }

    public void setFirstDay(DateMidnight firstDay) {
        this.firstDay = firstDay;
    }

    public DateMidnight getLastDay() {
        return lastDay;
    }

    public void setLastDay(DateMidnight lastDay) {
        this.lastDay = lastDay;
    }

    public boolean isMe() {
        return me;
    }

    public void setMe(boolean me) {
        this.me = me;
    }
}
