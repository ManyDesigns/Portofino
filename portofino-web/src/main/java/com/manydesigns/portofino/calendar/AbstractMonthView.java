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

package com.manydesigns.portofino.calendar;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
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
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final DateTime referenceDateTime;
    final int firstDayOfWeek;
    final DateMidnight referenceDateMidnight;
    final int referenceYear;
    final int referenceMonth;

    final DateMidnight monthStart;
    final DateMidnight monthEnd;
    final Interval monthInterval;

    final DateMidnight monthViewStart;
    final DateMidnight monthViewEnd;
    protected final Interval monthViewInterval;

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

        referenceDateMidnight = new DateMidnight(referenceDateTime);
        referenceYear = referenceDateTime.getYear();
        referenceMonth = referenceDateTime.getMonthOfYear();

        monthStart = referenceDateMidnight.withDayOfMonth(1);
        monthEnd = monthStart.plusMonths(1);
        monthInterval = new Interval(monthStart, monthEnd);

        monthViewStart = monthStart.withDayOfWeek(firstDayOfWeek);
        monthViewEnd = monthViewStart.plusWeeks(6);
        monthViewInterval = new Interval(monthViewStart, monthViewEnd);
        logger.debug("Month view start: {}", monthViewStart);



        logger.debug("Initializing weeks");
        weeks = createWeeksArray(6);
        DateMidnight weekStart = monthViewStart;
        for (int i = 0; i < weeks.length; i++) {
            DateMidnight weekEnd = weekStart.plusWeeks(1);
            weeks[i] = createWeek(weekStart, weekEnd);

            weekStart = weekEnd;
        }
    }

    protected abstract T[] createWeeksArray(int size);

    protected abstract T createWeek(DateMidnight weekStart, DateMidnight weekEnd);

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


    public DateMidnight getReferenceDateMidnight() {
        return referenceDateMidnight;
    }

    public DateMidnight getMonthStart() {
        return monthStart;
    }

    public DateMidnight getMonthEnd() {
        return monthEnd;
    }

    public Interval getMonthInterval() {
        return monthInterval;
    }

    public Interval getMonthViewInterval() {
        return monthViewInterval;
    }

    public DateMidnight getMonthViewStart() {
        return monthViewStart;
    }

    public DateMidnight getMonthViewEnd() {
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
