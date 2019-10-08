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
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.DateFormat;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.servlet.MutableHttpServletRequest;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.TimeZone;

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Test
public class JodaTimeFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2014, ManyDesigns srl";

    private JodaTimeField dateField;

    @DateFormat("yyyy-MM-dd")
    public DateTime date;

    @Ignore
    public void testDSTSwitch() throws NoSuchFieldException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Rome"));
        setupFields(Mode.EDIT);
        MutableHttpServletRequest request = new MutableHttpServletRequest();

        //Start from string

        //Compatibility
        String strDate = "1973-06-02";
        request.setParameter("date", strDate);
        dateField.readFromRequest(request);
        assertTrue(dateField.validate());
        dateField.writeToObject(this);
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(strDate);
        assertEquals(date, dateTime);
        assertEquals(strDate, dateField.getStringValue());

        assertEquals(1973, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(2, date.getDayOfMonth());
        assertEquals(0, date.getHourOfDay());
        assertEquals(0, date.getMinuteOfHour());
        assertEquals(0, date.getSecondOfMinute());
        System.out.println(date);
        dateField.readFromObject(this);
        assertEquals(date, dateField.getValue());
        assertEquals(strDate, dateField.getStringValue());

        //Daylight saving test
        strDate = "1973-06-03";
        request.setParameter("date", strDate);
        dateField.readFromRequest(request);
        assertTrue(dateField.validate());
        dateField.writeToObject(this);
        assertEquals(1973, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(3, date.getDayOfMonth());
        assertEquals(1, date.getHourOfDay());
        assertEquals(0, date.getMinuteOfHour());
        assertEquals(0, date.getSecondOfMinute());

        dateField.readFromObject(this);
        assertEquals(date, dateField.getValue());
        assertEquals(strDate, dateField.getStringValue());

        //Compatibility
        strDate = "1973-06-04";
        request.setParameter("date", strDate);
        dateField.readFromRequest(request);
        assertTrue(dateField.validate());
        dateField.writeToObject(this);
        dateTime = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(strDate);
        assertEquals(date, dateTime);
        assertEquals(1973, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(4, date.getDayOfMonth());
        assertEquals(0, date.getHourOfDay());
        assertEquals(0, date.getMinuteOfHour());
        assertEquals(0, date.getSecondOfMinute());
        System.out.println(date);
        dateField.readFromObject(this);
        assertEquals(date, dateField.getValue());
        assertEquals(strDate, dateField.getStringValue());

        //Start from date

        date = new DateTime().withYear(2014).withMonthOfYear(2).withDayOfMonth(5);
        dateField.readFromObject(this);
        assertEquals(date, dateField.getValue());
        assertEquals("2014-02-05", dateField.getStringValue());

        date = new DateTime().withYear(1973).withMonthOfYear(6).withDayOfMonth(3);
        dateField.readFromObject(this);
        assertEquals(date, dateField.getValue());
        assertEquals("1973-06-03", dateField.getStringValue());
    }

    private void setupFields(Mode mode) throws NoSuchFieldException {
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(this.getClass());
            PropertyAccessor myPropertyAccessor = classAccessor.getProperty("date");
            dateField = new JodaTimeField(myPropertyAccessor, mode, null);
    }
}
