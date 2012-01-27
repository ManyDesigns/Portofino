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

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MonthView {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    enum Search {
        BEFORE, DURING, AFTER
    }

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

    public MonthView(DateTime referenceDateTime) {
        this(referenceDateTime, DateTimeConstants.MONDAY);
    }

    public MonthView(DateTime referenceDateTime, int firstDayOfWeek) {
        logger.debug("Initializing MonthView");
        this.referenceDateTime = referenceDateTime;
        logger.debug("Today: {}", referenceDateTime);
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
        weeks = new Week[6];
        DateMidnight weekStart = monthViewStart;
        for (int i = 0; i < weeks.length; i++) {
            DateMidnight weekEnd = weekStart.plusWeeks(1);
            weeks[i] = new Week(weekStart, weekEnd);

            weekStart = weekEnd;
        }
    }

    //--------------------------------------------------------------------------
    // Events
    //--------------------------------------------------------------------------

    public int addEvents(Collection<Event> events) {
        int counter = 0;
        for (Event event : events) {
            boolean result = addEvent(event);
            if (result) {
                counter++;
            }
        }
        logger.debug("Added {} events", counter);
        return counter;
    }

    public boolean addEvent(Event event) {
        Interval monthViewOverlap =
                monthViewInterval.overlap(event.getInterval());
        if (monthViewOverlap == null) {
            logger.debug("Event not overlapping with month view");
            return false;
        } else {
            logger.debug("Event overlapping with month view");
            boolean weekSanityCheck = false;
            for (Week week : weeks) {
                weekSanityCheck = week.addEvent(event) || weekSanityCheck;
            }
            if (!weekSanityCheck) {
                logger.warn("Sanity check failed: Event overlaps with month but not with month's weeks.");
            }
            return true;
        }
    }

    public void clearEvents() {
        logger.debug("Clearing events");
        for (Week week : weeks) {
            week.clearEvents();
        }
    }

    public void sortEvents() {
        logger.debug("Sorting events");
        for (Week week : weeks) {
            week.sortEvents();
        }
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

    public DateTime getReferenceDateTime() {
        return referenceDateTime;
    }

    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    //--------------------------------------------------------------------------
    // Accessory classes
    //--------------------------------------------------------------------------

    public class Week {
        final DateMidnight weekStart;
        final DateMidnight weekEnd;
        final Interval weekInterval;
        final Day[] days;
        final List<EventWeek> eventWeekOverlaps;

        public Week(DateMidnight weekStart, DateMidnight weekEnd) {
            eventWeekOverlaps = new ArrayList<EventWeek>();
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

        public void clearEvents() {
            eventWeekOverlaps.clear();
        }

        public boolean addEvent(Event event) {
            Interval eventInterval = event.getInterval();
            Interval weekOverlap = weekInterval.overlap(eventInterval);
            if (weekOverlap == null) {
                logger.debug("Event not overlapping with week");
                return false;
            } else {
                logger.debug("Event overlapping with week.");
                logger.debug("Iterating on the days.");
                Integer startDay = null;
                Integer endDay = null;
                Search search = Search.BEFORE;
                for (int i = 0; i < 7; i++) {
                    Day day = days[i];
                    Interval dayInterval = day.getDayInterval();
                    Interval dayOverlap = dayInterval.overlap(eventInterval);
                    if (dayOverlap == null) {
                        logger.debug("Event not overlapping with day");
                        if (search == Search.DURING) {
                            logger.debug("Event end day found");
                            endDay = i - 1;
                            search = Search.AFTER;
                        }
                    } else {
                        logger.debug("Event overlapping with day");
                        if (search == Search.BEFORE) {
                            logger.debug("Event start day found");
                            startDay = i;
                            search = Search.DURING;
                        }
                    }
                }

                switch (search) {
                    case BEFORE:
                        logger.warn("Day range search internal error");
                        return false;
                    case DURING:
                        endDay = 6;
                        break;
                    default:
                        /* NOTHING */
                }
                if (startDay == null) {
                    logger.warn("Start day null");
                    return false;
                }
                if (endDay == null) {
                    logger.warn("End day null");
                    return false;
                }

                boolean continues =
                        event.getInterval().getEnd().isAfter(weekEnd);

                EventWeek eventWeek =
                        new EventWeek(event, startDay, endDay, continues);
                eventWeekOverlaps.add(eventWeek);
                return true;
            }
        }

        public List<EventWeek> getEventWeekOverlaps() {
            return eventWeekOverlaps;
        }

        public void sortEvents() {
            for (Day day : days) {
                day.clearSlots();
            }

            for (EventWeek current : eventWeekOverlaps) {
                Set<Integer> busySlots = new HashSet<Integer>();
                int startDay = current.getStartDay();
                int endDay = current.getEndDay();

                logger.debug("Querying days for busy slots");
                for (int i = startDay; i <= endDay; i++) {
                    days[i].fillBusySlots(busySlots);
                }

                logger.debug("Looking for first empty slot");
                int index = 0;
                boolean found = false;
                while (!found) {
                    if (busySlots.contains(index)) {
                        logger.debug("Slot {} busy", index);
                        index++;
                    } else {
                        logger.debug("Found empty slot: {}", index);
                        found = true;
                    }
                }

                logger.debug("Allocating slot");
                for (int i = startDay; i <= endDay; i++) {
                    days[i].allocateSlot(index, current);
                }
            }
        }
    }

    public class Day {
        final DateMidnight dayStart;
        final DateMidnight dayEnd;
        final Interval dayInterval;
        final boolean inReferenceMonth;
        final List<EventWeek> slots;

        public Day(DateMidnight dayStart, DateMidnight dayEnd) {
            this.dayStart = dayStart;
            this.dayEnd = dayEnd;
            dayInterval = new Interval(dayStart, dayEnd);
            inReferenceMonth = monthInterval.contains(dayStart);
            logger.debug("Day interval: {}", dayInterval);
            slots = new ArrayList<EventWeek>();
        }

        public void clearSlots() {
            slots.clear();
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

        public boolean isInReferenceMonth() {
            return inReferenceMonth;
        }

        public void fillBusySlots(Set<Integer> busySlots) {
            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i) == null) {
                    logger.debug("Empty slot {}", i);
                } else {
                    logger.debug("Busy slot {}", i);
                    busySlots.add(i);
                }
            }
        }

        public void allocateSlot(int index, EventWeek current) {
            ensureSlotsSize(index + 1);
            slots.set(index, current);
        }

        private void ensureSlotsSize(int requiredSize) {
            while (slots.size() < requiredSize) {
                slots.add(null);
            }
        }
    }
}
