/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
