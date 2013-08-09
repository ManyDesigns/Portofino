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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.PropertyAccessor;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParsePosition;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class NumericField extends AbstractTextField {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected BigDecimal decimalValue;

    protected DecimalFormat decimalFormat;
    protected int precision;
    protected int scale;
    protected BigDecimal minValue;
    protected BigDecimal maxValue;
    protected boolean decimalFormatError;

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public NumericField(PropertyAccessor accessor, Mode mode, String prefix) {
        super(accessor, mode, prefix);

        Class type = accessor.getType();
        if (type == Byte.class || type == Byte.TYPE) {
            minValue = new BigDecimal(Byte.MIN_VALUE);
            maxValue = new BigDecimal(Byte.MAX_VALUE);
        } else if (type == Short.class || type == Short.TYPE) {
            minValue = new BigDecimal(Short.MIN_VALUE);
            maxValue = new BigDecimal(Short.MAX_VALUE);
        } else if (type == Integer.class || type == Integer.TYPE) {
            minValue = new BigDecimal(Integer.MIN_VALUE);
            maxValue = new BigDecimal(Integer.MAX_VALUE);
        } else if (type == Long.class || type == Long.TYPE) {
            minValue = new BigDecimal(Long.MIN_VALUE);
            maxValue = new BigDecimal(Long.MAX_VALUE);
        }
        if (type.isPrimitive()) {
            setRequired(true);
        }

        if (accessor.isAnnotationPresent(PrecisionScale.class)) {
            PrecisionScale annotation =
                    accessor.getAnnotation(PrecisionScale.class);
            precision = annotation.precision();
            scale = annotation.scale();
            BigInteger unscaledMaxValue =
                    BigInteger.TEN.pow(precision).subtract(BigInteger.ONE);
            BigDecimal absMaxValue = new BigDecimal(unscaledMaxValue, scale);
            maxValue = absMaxValue;
            minValue = absMaxValue.negate();
        }

        // gestione valore minimo
        if (accessor.isAnnotationPresent(MinDecimalValue.class)) {
            double minDecimalValue =
                    accessor.getAnnotation(MinDecimalValue.class).value();
            minValue = new BigDecimal(minDecimalValue);
        } else if (accessor.isAnnotationPresent(MinIntValue.class)) {
            int minIntValue =
                    accessor.getAnnotation(MinIntValue.class).value();
            minValue = new BigDecimal(minIntValue);
        }

        // gestione valore massimo
        if (accessor.isAnnotationPresent(MaxDecimalValue.class)) {
            double maxDecimalValue =
                    accessor.getAnnotation(MaxDecimalValue.class).value();
            maxValue = new BigDecimal(maxDecimalValue);
        } else if (accessor.isAnnotationPresent(MaxIntValue.class)) {
            int maxIntValue =
                    accessor.getAnnotation(MaxIntValue.class).value();
            maxValue = new BigDecimal(maxIntValue);
        }

        if (accessor.isAnnotationPresent(com.manydesigns.elements.annotations.Memory.class)) {
            decimalFormat = new DecimalFormat("#,##0");
        }

        com.manydesigns.elements.annotations.DecimalFormat decimalFormatAnnotation =
                accessor.getAnnotation(com.manydesigns.elements.annotations.DecimalFormat.class);
        if (decimalFormatAnnotation != null) {
            decimalFormat = new DecimalFormat(decimalFormatAnnotation.value());
        }
    }

    //**************************************************************************
    // Implementazione di Component
    //**************************************************************************
    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);

        if (mode.isView(insertable, updatable)) {
            return;
        }

        String reqValue = req.getParameter(inputName);
        if (reqValue == null) {
            return;
        }

        stringValue = reqValue.trim();
        decimalValue = null;
        decimalFormatError = false;

        if (stringValue.length() == 0) {
            return;
        }

        BigDecimal tmpValue;
        try {
            //Provo a parserizzare il numero come BigDecimal
            tmpValue = (BigDecimal) OgnlUtils.convertValue(
                    stringValue, BigDecimal.class);
        } catch (Throwable e) {
            //Se il testo non e' un BigDecimal provo a parserizzarlo
            // con il pattern specificato nel format
            if (decimalFormat == null) {
                decimalFormatError = true;
                return;
            } else {
                decimalFormat.setParseBigDecimal(true);
                ParsePosition parsePos = new ParsePosition(0);
                tmpValue = (BigDecimal) decimalFormat.parse(stringValue, parsePos);
                if (stringValue.length() != parsePos.getIndex()) {
                    decimalFormatError = true;
                    return;
                }
            }
        }
        decimalValue = tmpValue.setScale(scale, BigDecimal.ROUND_HALF_EVEN);
    }

    @Override
    public boolean validate() {
        if (mode.isView(insertable, updatable) || (mode.isBulk() && !bulkChecked)) {
            return true;
        }

        if (!super.validate()) {
            return false;
        }
        if (stringValue == null || stringValue.length() == 0) {
            return true;
        }
        if (decimalFormatError) {
            errors.add(getText("elements.error.field.decimal.format"));
            return false;
        }
        if (minValue != null && decimalValue.compareTo(minValue) < 0) {
            errors.add(getText("elements.error.field.greater.or.equal", minValue));
            return false;
        }
        if (maxValue != null && decimalValue.compareTo(maxValue) > 0) {
            errors.add(getText("elements.error.field.less.or.equal", maxValue));
            return false;
        }
        return true;
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);

        if (obj == null) {
            decimalValue = null;
        } else {
            Object value = accessor.get(obj);
            decimalValue = (BigDecimal)
                    OgnlUtils.convertValue(value, BigDecimal.class);
        }

        if (decimalValue == null) {
            stringValue = null;
        } else if (decimalFormat == null) {
            stringValue = OgnlUtils.convertValueToString(decimalValue);
        } else {
            stringValue = decimalFormat.format(decimalValue);
        }
    }

    public void writeToObject(Object obj) {
        Class type = accessor.getType();
        Object value = OgnlUtils.convertValue(decimalValue, type);
        writeToObject(obj, value);
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public void setMaxValue(Double maxValue) {
        if (maxValue == null) {
            this.maxValue = null;
        } else {
            this.maxValue = new BigDecimal(maxValue);
        }
    }

    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    public void setDecimalFormat(DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public BigDecimal getValue() {
        return decimalValue;
    }

    public void setValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }
}
