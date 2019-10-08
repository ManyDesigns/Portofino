/*
 * Copyright (C) 2005-2014 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.annotations.DateFormat;
import com.manydesigns.elements.fields.search.BaseCriteria;
import com.manydesigns.elements.fields.search.DateSearchField;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.servlet.MutableHttpServletRequest;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Test
public class DateSearchFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2014, ManyDesigns srl";

    private DateSearchField dateField;

    @DateFormat("yyyy-MM-dd")
    public Date date;

    public void testRange() {
        setupFields();
        MutableHttpServletRequest request = new MutableHttpServletRequest();

        String minDate = "1999-01-01";
        String maxDate = "1999-02-01";
        request.setParameter("date_min", minDate);
        request.setParameter("date_max", maxDate);
        dateField.readFromRequest(request);
        assertTrue(dateField.validate());
        date = (Date) dateField.getMinValue();
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(minDate);
        assertEquals(new DateTime(date.getTime()), dateTime);
        assertEquals(minDate, dateField.getMinStringValue());
        date = (Date) dateField.getMaxValue();
        dateTime = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(maxDate);
        assertEquals(new DateTime(date.getTime()), dateTime);
        assertEquals(maxDate, dateField.getMaxStringValue());

        BaseCriteria criteria = new BaseCriteria();
        dateField.configureCriteria(criteria);
        assertEquals (dateField.getMinValue(), ((BaseCriteria.BetweenCriterion) criteria.get(0)).getMin());
        assertEquals (dateField.getMaxValue(), ((BaseCriteria.BetweenCriterion) criteria.get(0)).getMax());

        //Invers
        request.setParameter("date_min", maxDate);
        request.setParameter("date_max", minDate);
        dateField.readFromRequest(request);
        assertTrue(dateField.validate());
        date = (Date) dateField.getMinValue();
        dateTime = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(minDate);
        assertEquals(new DateTime(date.getTime()), dateTime);
        assertEquals(maxDate, dateField.getMinStringValue());
        date = (Date) dateField.getMaxValue();
        dateTime = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(maxDate);
        assertEquals(new DateTime(date.getTime()), dateTime);
        assertEquals(minDate, dateField.getMaxStringValue());

        criteria = new BaseCriteria();
        dateField.configureCriteria(criteria);
        assertEquals (dateField.getMinValue(), ((BaseCriteria.BetweenCriterion) criteria.get(0)).getMin());
        assertEquals (dateField.getMaxValue(), ((BaseCriteria.BetweenCriterion) criteria.get(0)).getMax());
    }

    public void testDSTSwitch() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Rome"));
        setupFields();
        MutableHttpServletRequest request = new MutableHttpServletRequest();

        //Start from string

        //Compatibility
        String strDate = "1973-06-02";
        request.setParameter("date_min", strDate);
        dateField.readFromRequest(request);
        assertTrue(dateField.validate());
        date = (Date) dateField.getMinValue();
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(strDate);
        assertEquals(new DateTime(date.getTime()), dateTime);
        assertEquals(strDate, dateField.getMinStringValue());

        assertEquals(73, date.getYear());
        assertEquals(5, date.getMonth());
        assertEquals(2, date.getDate());
        assertEquals(0, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        /* TODO DateSearchField works differently from DateField
        dateField.setMinValue(date);
        assertEquals(date, dateField.getMinValue());
        assertEquals(strDate, dateField.getMinStringValue());
        */

        //Daylight saving test
        strDate = "1973-06-03";
        request.setParameter("date_min", strDate);
        dateField.readFromRequest(request);
        assertTrue(dateField.validate());
        date = (Date) dateField.getMinValue();
        DateTime test = new DateTime(date.getTime());
        assertEquals(1973, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
        assertEquals(1, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());

        assertEquals(73, date.getYear());
        assertEquals(5, date.getMonth());
        assertEquals(3, date.getDate());
        assertEquals(1, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        /* TODO DateSearchField works differently from DateField
        dateField.setMinValue(date);
        assertEquals(date, dateField.getMinValue());
        assertEquals(strDate, dateField.getMinStringValue());*/

        //Compatibility
        strDate = "1973-06-04";
        request.setParameter("date_min", strDate);
        dateField.readFromRequest(request);
        assertTrue(dateField.validate());
        date = (Date) dateField.getMinValue();
        dateTime = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(strDate);
        assertEquals(new DateTime(date.getTime()), dateTime);
        assertEquals(73, date.getYear());
        assertEquals(5, date.getMonth());
        assertEquals(4, date.getDate());
        assertEquals(0, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        /* TODO DateSearchField works differently from DateField
        dateField.setMinValue(date);
        assertEquals(date, dateField.getMinValue());
        assertEquals(strDate, dateField.getMinStringValue());*/

        //Start from date
        /* TODO DateSearchField works differently from DateField
        date = new Date(114, 1, 5);
        dateField.setMinValue(date);
        assertEquals(date, dateField.getMinValue());
        assertEquals("2014-02-05", dateField.getMinStringValue());

        date = new Date(73, 5, 3);
        dateField.setMinValue(date);
        assertEquals(date, dateField.getMinValue());
        assertEquals("1973-06-03", dateField.getMinStringValue());*/
    }

    private void setupFields() {
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(this.getClass());
        try {
            PropertyAccessor myPropertyAccessor =
                    classAccessor.getProperty("date");
            dateField = new DateSearchField(myPropertyAccessor);

        } catch (NoSuchFieldException e) {
            fail(e.getMessage(), e);
        }
    }
}
