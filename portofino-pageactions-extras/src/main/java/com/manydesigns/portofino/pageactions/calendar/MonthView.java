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

package com.manydesigns.portofino.pageactions.calendar;

import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MonthView {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final DateTime today;
    final int firstDayOfWeek;
    final DateMidnight todayMidnight;
    final DateMidnight monthStart;
    final DateMidnight monthViewStart;
    final DateMidnight monthEnd;
    final DateMidnight monthViewEnd;
    final Interval monthViewInterval;

    final Week[] weeks;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(MonthView.class);

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public MonthView(DateTime today) {
        this(today, DateTimeConstants.MONDAY);
    }

    public MonthView(DateTime today, int firstDayOfWeek) {
        logger.debug("Initializing MonthView");
        this.today = today;
        logger.debug("Today: {}", today);
        this.firstDayOfWeek = firstDayOfWeek;
        logger.debug("First day of week: {}", firstDayOfWeek);

        todayMidnight = new DateMidnight(today);

        monthStart = todayMidnight.withDayOfMonth(1);
        monthViewStart = monthStart.withDayOfWeek(firstDayOfWeek);
        logger.debug("Month view start: {}", monthViewStart);

        monthEnd = monthStart.plusMonths(1);
        monthViewEnd = monthViewStart.plusWeeks(6);

        monthViewInterval = new Interval(monthViewStart, monthViewEnd);

        logger.debug("Initializing weeks");
        weeks = new Week[6];
        DateMidnight weekStart = monthViewStart;
        for (int i = 0; i < weeks.length; i++) {
            DateMidnight weekEnd = weekStart.plusWeeks(1);
            weeks[i] = new Week(weekStart, weekEnd);

            weekStart = weekEnd;
        }
    }


    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public DateMidnight getTodayMidnight() {
        return todayMidnight;
    }

    public DateMidnight getMonthStart() {
        return monthStart;
    }

    public DateMidnight getMonthEnd() {
        return monthEnd;
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

    public Week getWeek(int index) {
        return weeks[index];
    }

    public DateTime getToday() {
        return today;
    }

    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    //--------------------------------------------------------------------------
    // Accessory classes
    //--------------------------------------------------------------------------

    public static class Week {
        final DateMidnight weekStart;
        final DateMidnight weekEnd;
        final Interval weekInterval;
        final Day[] days;

        public Week(DateMidnight weekStart, DateMidnight weekEnd) {
            this.weekStart = weekStart;
            this.weekEnd = weekEnd;
            weekInterval = new Interval(weekStart, weekEnd);
            logger.debug("Week interval: {}", weekInterval);

            logger.debug("Initializing days");
            days = new Day[7];
            DateMidnight dayStart = weekStart;
            for (int i = 0; i < 7; i++) {
                DateMidnight dayEnd = dayStart.plusDays(1);
                days[i] = new Day(dayStart, dayEnd);

                dayStart = dayEnd;
            }
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

        public Day getDay(int index) {
            return days[index];
        }
    }

    public static class Day {
        final DateMidnight dayStart;
        final DateMidnight dayEnd;
        final Interval dayInterval;

        public Day(DateMidnight dayStart, DateMidnight dayEnd) {
            this.dayStart = dayStart;
            this.dayEnd = dayEnd;
            dayInterval = new Interval(dayStart, dayEnd);
            logger.debug("Day interval: {}", dayInterval);
        }

        public DateMidnight getDayStart() {
            return dayStart;
        }

        public DateMidnight getDayEnd() {
            return dayEnd;
        }

        public Interval getDayInterval() {
            return dayInterval;
        }
    }
}
