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

package com.manydesigns.portofino.pageactions.timesheet.model;

import com.manydesigns.portofino.calendar.AbstractDay;
import com.manydesigns.portofino.calendar.AbstractMonth;
import org.joda.time.DateMidnight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MonthReportModel extends AbstractMonth<MonthReportModel.Day> {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";


    //**************************************************************************
    // Variables
    //**************************************************************************

    protected String personId;
    protected String personName;

    protected Node rootNode;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(MonthReportModel.class);


    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public MonthReportModel(DateMidnight referenceDateMidnight) {
        super(referenceDateMidnight);
    }

    //--------------------------------------------------------------------------
    // AbstractMonth implementation
    //--------------------------------------------------------------------------

    @Override
    protected Day[] createDaysArray(int size) {
        return new Day[size];
    }

    @Override
    protected Day createDay(DateMidnight dayStart, DateMidnight dayEnd) {
        return new Day(dayStart, dayEnd);
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    public Node createNode(String id, String name) {
        return new Node(id, name, getDaysCount());
    }

    //--------------------------------------------------------------------------
    // Accessors
    //--------------------------------------------------------------------------

    public Node getRootNode() {
        return rootNode;
    }

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------

    public static class Day extends AbstractDay {
        boolean nonWorking;

        public Day(DateMidnight dayStart, DateMidnight dayEnd) {
            super(dayStart, dayEnd);
        }

        public boolean isNonWorking() {
            return nonWorking;
        }

        public void setNonWorking(boolean nonWorking) {
            this.nonWorking = nonWorking;
        }
    }

    public static class Node {
        final List<Node> childNodes;
        final String     id;
        final String     name;
        final int[]      minutesArray;
        Color color;

        public Node(String id, String name, int daysCount) {
            this.id = id;
            this.name = name;
            childNodes = new ArrayList<Node>();
            minutesArray = new int[daysCount];
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<Node> getChildNodes() {
            return childNodes;
        }

        public int getMinutes(int index) {
            return minutesArray[index];
        }

        public void setMinutes(int index, int value) {
            minutesArray[index] = value;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public int getMinutesTotal() {
            int total = 0;
            for (int minutes : minutesArray) {
                total += minutes;
            }
            return total;
        }

        public void calculateMinutesFromChildNodes() {
            resetMinutes();

            for (Node current : childNodes) {
                assert current.minutesArray.length == minutesArray.length;
                for (int i = 0; i < minutesArray.length; i++) {
                    minutesArray[i] += current.minutesArray[i];
                }
            }
        }

        private void resetMinutes() {
            logger.debug("Resetting minutes array");
            for (int i = 0; i < minutesArray.length; i++) {
                minutesArray[0] = 0;
            }
        }
    }
}
