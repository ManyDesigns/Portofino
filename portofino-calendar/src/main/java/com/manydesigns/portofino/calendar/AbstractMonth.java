/*
 * Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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
import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class AbstractMonth<T extends AbstractDay> {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final LocalDate referenceDateMidnight;

    final LocalDate monthStart;
    final LocalDate monthEnd;
    final Interval monthInterval;
    final int daysCount;

    protected final T[] days;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractMonthView.class);

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public AbstractMonth(LocalDate referenceDateMidnight) {
        logger.debug("Initializing month");
        this.referenceDateMidnight = referenceDateMidnight;
        logger.debug("Reference date midnight: {}", referenceDateMidnight);

        monthStart = referenceDateMidnight.withDayOfMonth(1);
        monthEnd = monthStart.plusMonths(1);
        monthInterval = new Interval(monthStart.toDateTimeAtStartOfDay(), monthEnd.toDateTimeAtStartOfDay());
        logger.debug("Month interval: {}", monthInterval);

        daysCount = Days.daysIn(monthInterval).getDays();
        logger.debug("Initializing {} days", daysCount);

        days = createDaysArray(daysCount);
        LocalDate dayStart = monthStart;
        for (int i = 0; i < daysCount; i++) {
            LocalDate dayEnd = dayStart.plusDays(1);
            days[i] = createDay(dayStart, dayEnd);

            // advance to next day
            dayStart = dayEnd;
        }
    }

    protected abstract T[] createDaysArray(int size);

    protected abstract T createDay(LocalDate dayStart, LocalDate dayEnd);

    public T findDayByDate(@NotNull LocalDate localDate) {
        DateTime dateMidnight = localDate.toDateTimeAtStartOfDay();
        if (!monthInterval.contains(dateMidnight)) {
            logger.debug("Date not in month interval: {}", localDate);
            return null;
        }
        for (T current : days) {
            if (current.getDayInterval().contains(dateMidnight)) {
                return current;
            }
        }
        throw new InternalError("Date in month but not in month's days: " + localDate);
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------


    public LocalDate getReferenceDateMidnight() {
        return referenceDateMidnight;
    }

    public LocalDate getMonthStart() {
        return monthStart;
    }

    public LocalDate getMonthEnd() {
        return monthEnd;
    }

    public Interval getMonthInterval() {
        return monthInterval;
    }

    public int getDaysCount() {
        return daysCount;
    }

    public T getDay(int index) {
        return days[index];
    }

}
