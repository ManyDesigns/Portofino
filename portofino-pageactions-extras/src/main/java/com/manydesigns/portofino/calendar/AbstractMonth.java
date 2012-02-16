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
import org.joda.time.Days;
import org.joda.time.Interval;
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
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final DateMidnight referenceDateMidnight;

    final DateMidnight monthStart;
    final DateMidnight monthEnd;
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

    public AbstractMonth(DateMidnight referenceDateMidnight) {
        logger.debug("Initializing month");
        this.referenceDateMidnight = referenceDateMidnight;
        logger.debug("Reference date midnight: {}", referenceDateMidnight);

        monthStart = referenceDateMidnight.withDayOfMonth(1);
        monthEnd = monthStart.plusMonths(1);
        monthInterval = new Interval(monthStart, monthEnd);
        logger.debug("Month interval: {}", monthInterval);

        daysCount = Days.daysIn(monthInterval).getDays();
        logger.debug("Initializing {} days", daysCount);

        days = createDaysArray(daysCount);
        DateMidnight dayStart = monthStart;
        for (int i = 0; i < daysCount; i++) {
            DateMidnight dayEnd = dayStart.plusDays(1);
            days[i] = createDay(dayStart, dayEnd);

            // advance to next day
            dayStart = dayEnd;
        }
    }

    protected abstract T[] createDaysArray(int size);

    protected abstract T createDay(DateMidnight dayStart, DateMidnight dayEnd);

    public T findDayByDate(@NotNull DateMidnight dateMidnight) {
        if (!monthInterval.contains(dateMidnight)) {
            logger.debug("Date not in month interval: {}", dateMidnight);
            return null;
        }
        for (T current : days) {
            if (current.getDayInterval().contains(dateMidnight)) {
                return current;
            }
        }
        throw new InternalError("Date in month but not in month's days: " + dateMidnight);
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

    public int getDaysCount() {
        return daysCount;
    }

    public T getDay(int index) {
        return days[index];
    }

}
