/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.calendar;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractWeek<U extends AbstractDay> {

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final DateMidnight weekStart;
    protected final DateMidnight weekEnd;
    protected final Interval weekInterval;
    protected final U[] days;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractWeek.class);

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public AbstractWeek(DateMidnight weekStart, DateMidnight weekEnd) {
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        weekInterval = new Interval(weekStart, weekEnd);
        AbstractMonthView.logger.debug("Week interval: {}", weekInterval);

        AbstractMonthView.logger.debug("Initializing days");
        days = createDaysArray(7);
        DateMidnight dayStart = weekStart;
        for (int i = 0; i < 7; i++) {
            DateMidnight dayEnd = dayStart.plusDays(1);
            days[i] = createDay(dayStart, dayEnd);

            dayStart = dayEnd;
        }
    }

    protected abstract U[] createDaysArray(int size);

    protected abstract U createDay(DateMidnight dayStart, DateMidnight dayEnd);

    public U findDayByDateTime(@NotNull DateTime dateTime) {
        if (!weekInterval.contains(dateTime)) {
            logger.debug("DateTime not in week internal: {}", dateTime);
            return null;
        }
        for (U current : days) {
            if (current.getDayInterval().contains(dateTime)) {
                return current;
            }
        }
        throw new InternalError("DateTime in week but not in week's days: " + dateTime);
    }


    public DateMidnight getWeekStart() {
        return weekStart;
    }

    public DateMidnight getWeekEnd() {
        return weekEnd;
    }

    public Interval getWeekInterval() {
        return weekInterval;
    }

    public U getDay(int index) {
        return days[index];
    }
}
