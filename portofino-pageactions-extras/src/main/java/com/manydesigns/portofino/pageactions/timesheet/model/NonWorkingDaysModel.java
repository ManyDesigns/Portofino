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

import com.manydesigns.portofino.calendar.AbstractMonthView;
import com.manydesigns.portofino.calendar.AbstractDay;
import com.manydesigns.portofino.calendar.AbstractWeek;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class NonWorkingDaysModel extends AbstractMonthView<NonWorkingDaysModel.NWDWeek> {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public NonWorkingDaysModel(DateTime referenceDateTime) {
        super(referenceDateTime);
    }

    public NonWorkingDaysModel(DateTime referenceDateTime, int firstDayOfWeek) {
        super(referenceDateTime, firstDayOfWeek);
    }

    @Override
    protected NWDWeek[] createWeeksArray(int size) {
        return new NWDWeek[size];
    }

    @Override
    protected NWDWeek createWeek(DateMidnight weekStart, DateMidnight weekEnd) {
        return new NWDWeek(weekStart, weekEnd);
    }

    public NWDDay findDayByDateTime(DateTime dateTime) {
        NWDWeek week = findWeekByDateTime(dateTime);
        if (week == null) {
            return null;
        }
        return week.findDayByDateTime(dateTime);
    }

    public class NWDWeek extends AbstractWeek<NWDDay> {
        public NWDWeek(DateMidnight weekStart, DateMidnight weekEnd) {
            super(weekStart, weekEnd);
        }

        @Override
        protected NWDDay[] createDaysArray(int size) {
            return new NWDDay[size];
        }

        @Override
        protected NWDDay createDay(DateMidnight dayStart, DateMidnight dayEnd) {
            return new NWDDay(dayStart, dayEnd);
        }

    }

    public class NWDDay extends AbstractDay {
        protected boolean nonWorking;

        public NWDDay(DateMidnight dayStart, DateMidnight dayEnd) {
            super(dayStart, dayEnd);
        }

        public boolean isNonWorking() {
            return nonWorking;
        }

        public void setNonWorking(boolean nonWorking) {
            this.nonWorking = nonWorking;
        }
    }
}
