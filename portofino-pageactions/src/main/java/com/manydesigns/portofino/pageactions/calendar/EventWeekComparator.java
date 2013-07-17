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

package com.manydesigns.portofino.pageactions.calendar;

import com.manydesigns.elements.util.StringComparator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import java.util.Comparator;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class EventWeekComparator implements Comparator<EventWeek> {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    final DateTimeComparator dateTimeComparator;
    final StringComparator stringComparator;

    public EventWeekComparator() {
        dateTimeComparator = DateTimeComparator.getInstance();
        stringComparator = new StringComparator();
    }

    public int compare(EventWeek eventWeek1, EventWeek eventWeek2) {
        Event event1 = eventWeek1.getEvent();
        DateTime startDateTime1 = event1.getInterval().getStart();

        Event event2 = eventWeek2.getEvent();
        DateTime startDateTime2 = event2.getInterval().getStart();

        int result = dateTimeComparator.compare(startDateTime1, startDateTime2);
        if (result == 0) {
            return stringComparator.compare(event1.getId(), event2.getId());
        } else {
            return result;
        }
    }
}
