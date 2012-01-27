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

import junit.framework.TestCase;
import org.joda.time.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MonthViewTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    DateTimeZone dtz = DateTimeZone.UTC;

    MonthView monthView;

    public void testConstructor() throws Exception {
        DateMidnight dec26 = new DateMidnight(
                2011, DateTimeConstants.DECEMBER, 26,
                dtz);
        DateMidnight dec27 = dec26.plusDays(1);
        DateMidnight dec28 = dec27.plusDays(1);
        DateMidnight dec29 = dec28.plusDays(1);
        DateMidnight dec30 = dec29.plusDays(1);
        DateMidnight dec31 = dec30.plusDays(1);
        DateMidnight jan1 = dec31.plusDays(1);
        DateMidnight jan2 = jan1.plusDays(1);
        DateMidnight jan9 = jan2.plusDays(7);

        DateMidnight feb1 = new DateMidnight(
                2012, DateTimeConstants.FEBRUARY, 1,
                dtz);

        DateMidnight feb6 = new DateMidnight(
                2012, DateTimeConstants.FEBRUARY, 6,
                dtz);

        DateTime today = new DateTime(
                2012, DateTimeConstants.JANUARY, 27,
                11, 4, 35, 737,
                dtz);

        // creating the month view
        monthView = new MonthView(today);
        assertEquals(today, monthView.getToday());
        assertEquals(DateTimeConstants.MONDAY, monthView.getFirstDayOfWeek());

        // month start/end
        assertEquals(jan1, monthView.getMonthStart());
        assertEquals(feb1, monthView.getMonthEnd());

        // month view start/end
        assertEquals(dec26, monthView.getMonthViewStart());
        assertEquals(feb6, monthView.getMonthViewEnd());

        // monthview interval
        Interval monthViewInterval = monthView.getMonthViewInterval();
        assertEquals(dec26.getMillis(), monthViewInterval.getStartMillis());
        assertEquals(feb6.getMillis(), monthViewInterval.getEndMillis());


        MonthView.Week week = monthView.getWeek(0);
        assertEquals(dec26, week.getWeekStart());
        assertEquals(jan2, week.getWeekEnd());

        MonthView.Day day = week.getDay(0);
        assertEquals(dec26, day.getDayStart());
        assertEquals(dec27, day.getDayEnd());

        day = week.getDay(1);
        assertEquals(dec27, day.getDayStart());
        assertEquals(dec28, day.getDayEnd());

        day = week.getDay(6);
        assertEquals(jan1, day.getDayStart());
        assertEquals(jan2, day.getDayEnd());

        week = monthView.getWeek(1);
        assertEquals(jan2, week.getWeekStart());
        assertEquals(jan9, week.getWeekEnd());

        day = week.getDay(0);
        assertEquals(jan2, day.getDayStart());
    }


    public void testMonthStartsOnMonday() throws Exception {
        DateTime today = new DateTime(
                2012, DateTimeConstants.OCTOBER, 1,
                12, 0, 0, 0,
                dtz);
        monthView = new MonthView(today);

        DateMidnight expectedMonthStart = new DateMidnight(
                2012, DateTimeConstants.OCTOBER, 1,
                dtz);
        assertEquals(expectedMonthStart, monthView.getMonthStart());
        assertEquals(expectedMonthStart, monthView.getMonthViewStart());

        DateMidnight expectedMonthViewEnd = new DateMidnight(
                2012, DateTimeConstants.NOVEMBER, 12,
                dtz);
        assertEquals(expectedMonthViewEnd, monthView.getMonthViewEnd());
    }

    public void testMonthEndsOnSunday() throws Exception {
        DateTime today = new DateTime(
                2012, DateTimeConstants.OCTOBER, 1,
                12, 0, 0, 0,
                dtz);
        monthView = new MonthView(today);

        DateMidnight expectedMonthStart = new DateMidnight(
                2012, DateTimeConstants.OCTOBER, 1,
                dtz);
        assertEquals(expectedMonthStart, monthView.getMonthStart());
        assertEquals(expectedMonthStart, monthView.getMonthViewStart());
    }
}
