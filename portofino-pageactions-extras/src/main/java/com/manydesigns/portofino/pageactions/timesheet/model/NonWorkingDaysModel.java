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

import com.manydesigns.portofino.calendar.AbstractMonth;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class NonWorkingDaysModel extends AbstractMonth<NonWorkingDaysModel.NWDWeek> {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

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

    public class NWDWeek extends AbstractMonth<NonWorkingDaysModel.NWDWeek>.Week<NWDDay> {
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

    public class NWDDay extends AbstractMonth.Day {
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
