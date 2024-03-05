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
public abstract class AbstractMonthView<T extends AbstractWeek> {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final DateTime  referenceDateTime;
    final int       firstDayOfWeek;
    final LocalDate referenceDateMidnight;
    final int       referenceYear;
    final int       referenceMonth;

    final LocalDate monthStart;
    final LocalDate monthEnd;
    final Interval  monthInterval;

    final           LocalDate monthViewStart;
    final           LocalDate monthViewEnd;
    protected final Interval  monthViewInterval;

    protected final T[] weeks;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractMonthView.class);

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public AbstractMonthView(DateTime referenceDateTime) {
        this(referenceDateTime, DateTimeConstants.MONDAY);
    }

    public AbstractMonthView(DateTime referenceDateTime, int firstDayOfWeek) {
        logger.debug("Initializing month");
        this.referenceDateTime = referenceDateTime;
        logger.debug("Reference date time: {}", referenceDateTime);
        this.firstDayOfWeek = firstDayOfWeek;
        logger.debug("First day of week: {}", firstDayOfWeek);

        referenceDateMidnight = new LocalDate(referenceDateTime);
        referenceYear = referenceDateTime.getYear();
        referenceMonth = referenceDateTime.getMonthOfYear();

        monthStart = referenceDateMidnight.withDayOfMonth(1);
        monthEnd = monthStart.plusMonths(1);
        monthInterval = new Interval(monthStart.toDateTimeAtStartOfDay(), monthEnd.toDateTimeAtStartOfDay());

        monthViewStart = monthStart.withDayOfWeek(firstDayOfWeek);
        monthViewEnd = monthViewStart.plusWeeks(6);
        monthViewInterval = new Interval(monthViewStart.toDateTimeAtStartOfDay(), monthViewEnd.toDateTimeAtStartOfDay());
        logger.debug("Month view start: {}", monthViewStart);


        logger.debug("Initializing weeks");
        weeks = createWeeksArray(6);
        LocalDate weekStart = monthViewStart;
        for (int i = 0; i < weeks.length; i++) {
            LocalDate weekEnd = weekStart.plusWeeks(1);
            weeks[i] = createWeek(weekStart, weekEnd);

            weekStart = weekEnd;
        }
    }

    protected abstract T[] createWeeksArray(int size);

    protected abstract T createWeek(LocalDate weekStart, LocalDate weekEnd);

    public T findWeekByDateTime(@NotNull DateTime dateTime) {
        if (!monthViewInterval.contains(dateTime)) {
            logger.debug("DateTime not in monthView internal: {}", dateTime);
            return null;
        }
        for (T current : weeks) {
            if (current.getWeekInterval().contains(dateTime)) {
                return current;
            }
        }
        throw new InternalError("DateTime in month but not in month's weeks: " + dateTime);
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

    public Interval getMonthViewInterval() {
        return monthViewInterval;
    }

    public LocalDate getMonthViewStart() {
        return monthViewStart;
    }

    public LocalDate getMonthViewEnd() {
        return monthViewEnd;
    }

    public T getWeek(int index) {
        return weeks[index];
    }

    public DateTime getReferenceDateTime() {
        return referenceDateTime;
    }

    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

}
