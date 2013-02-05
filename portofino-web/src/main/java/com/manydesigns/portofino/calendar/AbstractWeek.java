/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
