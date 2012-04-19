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

package com.manydesigns.elements.util;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.ognl.OgnlUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class UtilTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public void testMatchStringArray1() {
        String[] result = Util.matchStringArray("\"qui\",\"quo\",\"qua\"");
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("qui", result[0]);
        assertEquals("quo", result[1]);
        assertEquals("qua", result[2]);
    }

    public void testMatchStringArray2() {
        String[] result = Util.matchStringArray(" \"qui\" , \"quo\" , \"qua\" ");
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("qui", result[0]);
        assertEquals("quo", result[1]);
        assertEquals("qua", result[2]);
    }

    public void testMatchStringArray3() {
        String[] result = Util.matchStringArray("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    public void testMatchStringArray4() {
        String[] result = Util.matchStringArray(" \"qui\" ");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("qui", result[0]);
    }

    public void testMatchStringArray5() {
        String[] result = Util.matchStringArray("\"qui\",");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("qui", result[0]);
        assertEquals("", result[1]);
    }

    public void testMatchStringArray6() {
        String[] result = Util.matchStringArray(",\"qui\"");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("", result[0]);
        assertEquals("qui", result[1]);
    }

    public void testMatchStringArray7() {
        String[] result = Util.matchStringArray("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    public void testMatchStringArray8() {
        String[] result = Util.matchStringArray("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }


    public void testMatchStringArray9() {
        String[] result = Util.matchStringArray("\"qu\"\"i\",\"qu\"\"o\",\"qu\"\"a\"");
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("qu\"i", result[0]);
        assertEquals("qu\"o", result[1]);
        assertEquals("qu\"a", result[2]);
    }


    public void testMatchStringArray10() {
        String[] result = Util.matchStringArray("qu i , q uo,q u a");
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("qu i", result[0]);
        assertEquals("q uo", result[1]);
        assertEquals("q u a", result[2]);
    }

    public void testConvertFromInteger() {
        int intValue = 3;

        byte byteValue = (Byte) OgnlUtils.convertValue(intValue, Byte.class);
        assertEquals((byte)3, byteValue);

        short shortValue = (Short) OgnlUtils.convertValue(intValue, Short.class);
        assertEquals((short)3, shortValue);

        long longValue = (Long) OgnlUtils.convertValue(intValue, Long.class);
        assertEquals((long)3, longValue);

        BigDecimal bigDecimalValue = (BigDecimal) OgnlUtils.convertValue(intValue, BigDecimal.class);
        assertEquals(new BigDecimal(3), bigDecimalValue);

        BigInteger bigIntegerValue = (BigInteger) OgnlUtils.convertValue(intValue, BigInteger.class);
        assertEquals(new BigInteger("3"), bigIntegerValue);

        float floatValue = (Float) OgnlUtils.convertValue(intValue, Float.class);
        assertEquals(3f, floatValue);

        double doubleValue = (Double) OgnlUtils.convertValue(intValue, Double.class);
        assertEquals(3.0, doubleValue);
    }

    public void testConvertFromBigDecimal() {
        BigDecimal bigDecimalValue = new BigDecimal(3);

        byte byteValue = (Byte) OgnlUtils.convertValue(bigDecimalValue, Byte.class);
        assertEquals((byte)3, byteValue);

        short shortValue = (Short) OgnlUtils.convertValue(bigDecimalValue, Short.class);
        assertEquals((short)3, shortValue);

        long longValue = (Long) OgnlUtils.convertValue(bigDecimalValue, Long.class);
        assertEquals((long)3, longValue);

        int intValue = (Integer) OgnlUtils.convertValue(bigDecimalValue, Integer.class);
        assertEquals(3, intValue);

        float floatValue = (Float) OgnlUtils.convertValue(bigDecimalValue, Float.class);
        assertEquals(3f, floatValue);

        double doubleValue = (Double) OgnlUtils.convertValue(bigDecimalValue, Double.class);
        assertEquals(3.0, doubleValue);

        BigInteger bigIntegerValue = (BigInteger) OgnlUtils.convertValue(bigDecimalValue, BigInteger.class);
        assertEquals(new BigInteger("3"), bigIntegerValue);
    }

    public void testIsNumericType() {
        assertTrue(Util.isNumericType(Integer.class));
        assertTrue(Util.isNumericType(Integer.TYPE));
        assertTrue(Util.isNumericType(Byte.class));
        assertTrue(Util.isNumericType(Byte.TYPE));
        assertTrue(Util.isNumericType(Short.class));
        assertTrue(Util.isNumericType(Short.TYPE));
        assertTrue(Util.isNumericType(Long.class));
        assertTrue(Util.isNumericType(Long.TYPE));
        assertTrue(Util.isNumericType(Float.class));
        assertTrue(Util.isNumericType(Float.TYPE));
        assertTrue(Util.isNumericType(Double.class));
        assertTrue(Util.isNumericType(Double.TYPE));
        assertTrue(Util.isNumericType(BigDecimal.class));
        assertTrue(Util.isNumericType(BigInteger.class));

        assertFalse(Util.isNumericType(String.class));
    }

    public void testConvert() {
        assertEquals((Integer)3, OgnlUtils.convertValue("3", Integer.class));

        try {
            OgnlUtils.convertValue("bla", Integer.class);
            fail();
        } catch (NumberFormatException e) {
            assertEquals("For input string: \"bla\"", e.getMessage());
        }

        try {
            OgnlUtils.convertValue("1.0bla", BigDecimal.class);
            fail();
        } catch (NumberFormatException e) {
            assertNull(e.getMessage());
        }

        //Testo la conversione da Timestamp a Date
        Date date = new Date();
        Timestamp target = new Timestamp(date.getTime());

        assertEquals(target, OgnlUtils.convertValue(date, Timestamp.class));
    }

}
