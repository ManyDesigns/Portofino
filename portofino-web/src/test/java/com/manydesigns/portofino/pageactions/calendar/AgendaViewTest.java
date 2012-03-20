/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
public class AgendaViewTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

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

    DateTime today = new DateTime(jan1);



    AgendaView agendaView;

    public void testConstructor() throws Exception {
        // creating the month view
        agendaView = new AgendaView(today);
        assertEquals(new DateMidnight(today), agendaView.getFirstDay());
    }


    public void testAddOneDayEvent() throws Exception {
        agendaView = new AgendaView(today);

        // create the event
        Calendar calendar = null;
        Interval eventInterval = new Interval(jan3, jan4);
        Event event = new Event(
                calendar, "e1", "Test event", eventInterval, null, null);

        assertEquals(1, agendaView.addEvent(event));
        assertEquals(1, agendaView.getEvents().size());

        EventDay eventDay = agendaView.getEvents().get(0);
        assertEquals(jan3, eventDay.getDay());
    }

    public void testAddTwoDaysEvent() throws Exception {
        agendaView = new AgendaView(today);

        // create the event
        Calendar calendar = null;
        Interval eventInterval = new Interval(jan3, jan5);
        Event event = new Event(
                calendar, "e1", "Test event", eventInterval, null, null);

        assertEquals(2, agendaView.addEvent(event));
        assertEquals(2, agendaView.getEvents().size());

        EventDay eventDay = agendaView.getEvents().get(0);
        assertEquals(jan3, eventDay.getDay());
        eventDay = agendaView.getEvents().get(1);
        assertEquals(jan4, eventDay.getDay());
    }

    public void testAddEventOnTwoWeeks() throws Exception {
        agendaView = new AgendaView(today);

        // create the event
        Calendar calendar = null;
        Interval eventInterval = new Interval(jan7, jan12);
        Event event = new Event(
                calendar, "e1", "Test event", eventInterval, null, null);

        assertEquals(5, agendaView.addEvent(event));
        assertEquals(5, agendaView.getEvents().size());

        EventDay eventDay = agendaView.getEvents().get(0);
        assertEquals(jan7, eventDay.getDay());
        eventDay = agendaView.getEvents().get(1);
        assertEquals(jan8, eventDay.getDay());
        eventDay = agendaView.getEvents().get(2);
        assertEquals(jan9, eventDay.getDay());
        eventDay = agendaView.getEvents().get(3);
        assertEquals(jan10, eventDay.getDay());
        eventDay = agendaView.getEvents().get(4);
        assertEquals(jan11, eventDay.getDay());
    }

    public void testSort() throws Exception {
        agendaView = new AgendaView(today);

        // create the event
        Calendar calendar = null;

        Interval eventInterval1 = new Interval(jan18, jan22);
        Event event1 = new Event(
                calendar, "e1", "Test event 1", eventInterval1, null, null);
        assertEquals(4, agendaView.addEvent(event1));

        Interval eventInterval2 = new Interval(jan20, jan21);
        Event event2 = new Event(
                calendar, "e2", "Test event 2", eventInterval2, null, null);
        assertEquals(1, agendaView.addEvent(event2));

        Interval eventInterval3 = new Interval(jan16, jan19);
        Event event3 = new Event(
                calendar, "e3", "Test event 3", eventInterval3, null, null);
        assertEquals(3, agendaView.addEvent(event3));

        assertEquals(6, agendaView.getEvents().size());

        agendaView.sortEvents();

        EventDay eventDay = agendaView.getEvents().get(0);
        assertEquals(jan16, eventDay.getDay());
        assertEquals(1, eventDay.getEvents().size());
        eventDay = agendaView.getEvents().get(1);
        assertEquals(jan17, eventDay.getDay());
        assertEquals(1, eventDay.getEvents().size());
        eventDay = agendaView.getEvents().get(2);
        assertEquals(jan18, eventDay.getDay());
        assertEquals(2, eventDay.getEvents().size());
        DateTime start1 = eventDay.getEvents().get(0).getInterval().getStart();
        DateTime start2 = eventDay.getEvents().get(1).getInterval().getStart();
        assertTrue(start1.compareTo(start2) <= 0);

        eventDay = agendaView.getEvents().get(3);
        assertEquals(jan19, eventDay.getDay());
        assertEquals(1, eventDay.getEvents().size());
        eventDay = agendaView.getEvents().get(4);
        assertEquals(jan20, eventDay.getDay());
        assertEquals(2, eventDay.getEvents().size());
        start1 = eventDay.getEvents().get(0).getInterval().getStart();
        start2 = eventDay.getEvents().get(1).getInterval().getStart();
        assertTrue(start1.compareTo(start2) <= 0);
        
        eventDay = agendaView.getEvents().get(5);
        assertEquals(jan21, eventDay.getDay());
        assertEquals(1, eventDay.getEvents().size());

    }
}
