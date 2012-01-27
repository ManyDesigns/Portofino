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

import java.util.List;

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
    DateMidnight jan3 = jan2.plusDays(1);
    DateMidnight jan4 = jan3.plusDays(1);
    DateMidnight jan5 = jan4.plusDays(1);
    DateMidnight jan6 = jan5.plusDays(1);
    DateMidnight jan7 = jan6.plusDays(1);
    DateMidnight jan8 = jan7.plusDays(1);
    DateMidnight jan9 = jan8.plusDays(1);
    DateMidnight jan10 = jan9.plusDays(1);
    DateMidnight jan11 = jan10.plusDays(1);
    DateMidnight jan12 = jan11.plusDays(1);
    DateMidnight jan13 = jan12.plusDays(1);
    DateMidnight jan14 = jan13.plusDays(1);
    DateMidnight jan15 = jan14.plusDays(1);
    DateMidnight jan16 = jan15.plusDays(1);
    DateMidnight jan17 = jan16.plusDays(1);
    DateMidnight jan18 = jan17.plusDays(1);
    DateMidnight jan19 = jan18.plusDays(1);
    DateMidnight jan20 = jan19.plusDays(1);
    DateMidnight jan21 = jan20.plusDays(1);
    DateMidnight jan22 = jan21.plusDays(1);
    DateMidnight jan23 = jan22.plusDays(1);
    DateMidnight jan24 = jan23.plusDays(1);
    DateMidnight jan25 = jan24.plusDays(1);
    DateMidnight jan26 = jan25.plusDays(1);
    DateMidnight jan27 = jan26.plusDays(1);
    DateMidnight jan28 = jan27.plusDays(1);
    DateMidnight jan29 = jan28.plusDays(1);
    DateMidnight jan30 = jan29.plusDays(1);
    DateMidnight jan31 = jan30.plusDays(1);
    DateMidnight feb1 = jan31.plusDays(1);
    DateMidnight feb2 = feb1.plusDays(1);
    DateMidnight feb3 = feb2.plusDays(1);
    DateMidnight feb4 = feb3.plusDays(1);
    DateMidnight feb5 = feb4.plusDays(1);
    DateMidnight feb6 = feb5.plusDays(1);

    DateTime today = new DateTime(
            2012, DateTimeConstants.JANUARY, 27,
            11, 4, 35, 737,
            dtz);



    MonthView monthView;

    public void testConstructor() throws Exception {
        // creating the month view
        monthView = new MonthView(today);
        assertEquals(today, monthView.getReferenceDateTime());
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

    public void testAddOneDayEvent() throws Exception {
        monthView = new MonthView(today);

        // create the event
        Calendar calendar = null;
        Interval eventInterval = new Interval(jan3, jan4);
        Event event = new Event(
                calendar, "e1", "Test event", eventInterval, null, null);

        assertTrue(monthView.addEvent(event));

        // first week is empty
        MonthView.Week week = monthView.getWeek(0);
        List<EventWeek> eventWeekOverlaps = week.getEventWeekOverlaps();
        assertEquals(0, eventWeekOverlaps.size());

        // the week containing the event
        week = monthView.getWeek(1);
        eventWeekOverlaps = week.getEventWeekOverlaps();
        assertEquals(1, eventWeekOverlaps.size());

        EventWeek eventWeek = eventWeekOverlaps.get(0);
        assertEquals(event, eventWeek.getEvent());
        assertEquals(1, eventWeek.getStartDay());
        assertEquals(1, eventWeek.getEndDay());
        assertFalse(eventWeek.isContinues());

        // the rest of the weeks are empty
        for (int i = 2; i < 6; i++) {
            week = monthView.getWeek(i);
            eventWeekOverlaps = week.getEventWeekOverlaps();
            assertEquals(0, eventWeekOverlaps.size());
        }
    }

    public void testAddTwoDaysEvent() throws Exception {
        monthView = new MonthView(today);

        // create the event
        Calendar calendar = null;
        DateTime start = jan3.toDateTime().plus(Hours.hours(15));
        DateTime end = jan4.toDateTime().plus(Hours.hours(9));
        Interval eventInterval = new Interval(jan3, jan5);
        Event event = new Event(
                calendar, "e1", "Test event", eventInterval, null, null);

        assertTrue(monthView.addEvent(event));

        // first week is empty
        MonthView.Week week = monthView.getWeek(0);
        List<EventWeek> eventWeekOverlaps = week.getEventWeekOverlaps();
        assertEquals(0, eventWeekOverlaps.size());

        // the week containing the event
        week = monthView.getWeek(1);
        eventWeekOverlaps = week.getEventWeekOverlaps();
        assertEquals(1, eventWeekOverlaps.size());

        EventWeek eventWeek = eventWeekOverlaps.get(0);
        assertEquals(event, eventWeek.getEvent());
        assertEquals(1, eventWeek.getStartDay());
        assertEquals(2, eventWeek.getEndDay());
        assertFalse(eventWeek.isContinues());

        // the rest of the weeks are empty
        for (int i = 2; i < 6; i++) {
            week = monthView.getWeek(i);
            eventWeekOverlaps = week.getEventWeekOverlaps();
            assertEquals(0, eventWeekOverlaps.size());
        }
    }

    public void testAddEventOnTwoWeeks() throws Exception {
        monthView = new MonthView(today);

        // create the event
        Calendar calendar = null;
        Interval eventInterval = new Interval(jan7, jan12);
        Event event = new Event(
                calendar, "e1", "Test event", eventInterval, null, null);

        assertTrue(monthView.addEvent(event));

        // first week is empty
        MonthView.Week week = monthView.getWeek(0);
        List<EventWeek> eventWeekOverlaps = week.getEventWeekOverlaps();
        assertEquals(0, eventWeekOverlaps.size());

        // the week containing the first part of the event
        week = monthView.getWeek(1);
        eventWeekOverlaps = week.getEventWeekOverlaps();
        assertEquals(1, eventWeekOverlaps.size());

        EventWeek eventWeek = eventWeekOverlaps.get(0);
        assertEquals(event, eventWeek.getEvent());
        assertEquals(5, eventWeek.getStartDay());
        assertEquals(6, eventWeek.getEndDay());
        assertTrue(eventWeek.isContinues());

        // the week containing the second part of the event
        week = monthView.getWeek(2);
        eventWeekOverlaps = week.getEventWeekOverlaps();
        assertEquals(1, eventWeekOverlaps.size());

        eventWeek = eventWeekOverlaps.get(0);
        assertEquals(event, eventWeek.getEvent());
        assertEquals(0, eventWeek.getStartDay());
        assertEquals(2, eventWeek.getEndDay());
        assertFalse(eventWeek.isContinues());

        // the rest of the weeks are empty
        for (int i = 3; i < 6; i++) {
            week = monthView.getWeek(i);
            eventWeekOverlaps = week.getEventWeekOverlaps();
            assertEquals(0, eventWeekOverlaps.size());
        }
    }

    public void testSort() throws Exception {
        monthView = new MonthView(today);

        // create the event
        Calendar calendar = null;

        Interval eventInterval1 = new Interval(jan18, jan22);
        Event event1 = new Event(
                calendar, "e1", "Test event 1", eventInterval1, null, null);
        assertTrue(monthView.addEvent(event1));

        Interval eventInterval2 = new Interval(jan20, jan21);
        Event event2 = new Event(
                calendar, "e2", "Test event 2", eventInterval2, null, null);
        assertTrue(monthView.addEvent(event2));

        Interval eventInterval3 = new Interval(jan16, jan19);
        Event event3 = new Event(
                calendar, "e3", "Test event 3", eventInterval3, null, null);
        assertTrue(monthView.addEvent(event3));

        monthView.sortEvents();

        MonthView.Week week = monthView.getWeek(3);

        MonthView.Day day = week.getDay(0);
        List<EventWeek> slots = day.slots;
        assertEquals(1, slots.size());
        assertEquals(event3, slots.get(0).getEvent());

        day = week.getDay(1);
        slots = day.slots;
        assertEquals(1, slots.size());
        assertEquals(event3, slots.get(0).getEvent());

        day = week.getDay(2);
        slots = day.slots;
        assertEquals(2, slots.size());
        assertEquals(event3, slots.get(0).getEvent());
        assertEquals(event1, slots.get(1).getEvent());

        day = week.getDay(3);
        slots = day.slots;
        assertEquals(2, slots.size());
        assertNull(slots.get(0));
        assertEquals(event1, slots.get(1).getEvent());

        day = week.getDay(4);
        slots = day.slots;
        assertEquals(2, slots.size());
        assertEquals(event2, slots.get(0).getEvent());
        assertEquals(event1, slots.get(1).getEvent());

        day = week.getDay(5);
        slots = day.slots;
        assertEquals(2, slots.size());
        assertNull(slots.get(0));
        assertEquals(event1, slots.get(1).getEvent());

        day = week.getDay(6);
        slots = day.slots;
        assertEquals(0, slots.size());

    }
}
