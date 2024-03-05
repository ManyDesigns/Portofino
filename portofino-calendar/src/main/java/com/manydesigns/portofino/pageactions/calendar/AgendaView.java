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

package com.manydesigns.portofino.pageactions.calendar;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class AgendaView {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    protected LocalDate firstDay;
    protected final List<EventDay> events = new LinkedList<EventDay>();
    
    public AgendaView(DateTime referenceDateTime) {
        firstDay = referenceDateTime.toLocalDate();
    }

    public int addEvent(Event event) {
        DateTime day = event.getInterval().getStart();
        DateTime end = event.getInterval().getEnd();
        int added = 0;
        while(end.minus(1).compareTo(day) >= 0) {
            if(addEvent(day.toLocalDate(), event)) {
                added++;
            }
            day = day.plusDays(1);
        }
        return added;
    }
    
    @Deprecated
    protected boolean addEvent(DateMidnight date, Event event) {
        return addEvent(date.toLocalDate(), event);
    }

    protected boolean addEvent(LocalDate date, Event event) {
        if(date.isBefore(firstDay)) {
            return false;
        }
        int position = 0;
        for(EventDay eventDay : events) {
            int cmp = eventDay.getDay().compareTo(date.toDateTimeAtStartOfDay());
            if(cmp == 0) {
                eventDay.getEvents().add(event);
                return true;
            } else if(cmp > 0) {
                EventDay newEventDay = new EventDay(date, event);
                events.add(position, newEventDay);
                return true;
            }
            position++;
        }
        EventDay newEventDay = new EventDay(date, event);
        events.add(newEventDay);
        return true;
    }

    public void sortEvents() {
        for(EventDay eventDay : events) {
            Collections.sort(eventDay.getEvents(), new Comparator<Event>() {
                public int compare(Event o1, Event o2) {
                    return o1.getInterval().getStart().compareTo(o2.getInterval().getStart());
                }
            });
        }
    }

    public List<EventDay> getEvents() {
        return events;
    }

    public LocalDate getFirstDay() {
        return firstDay;
    }
}
