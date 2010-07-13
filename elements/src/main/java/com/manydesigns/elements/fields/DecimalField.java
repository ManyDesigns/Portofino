/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.MaxDecimalValue;
import com.manydesigns.elements.annotations.MinDecimalValue;
import com.manydesigns.elements.annotations.PrecisionScale;
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
*/
public class DecimalField extends AbstractTextField {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected BigDecimal decimalValue;

    protected DecimalFormat decimalFormat;
    protected int precision;
    protected int scale;
    protected BigDecimal minValue;
    protected BigDecimal maxValue;
    protected boolean decimalFormatError;

    //--------------------------------------------------------------------------
    // Costruttori
    //--------------------------------------------------------------------------
    public DecimalField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public DecimalField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);
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
        if (accessor.isAnnotationPresent(MinDecimalValue.class)) {
            setMinValue(accessor.getAnnotation(MinDecimalValue.class).value());
        }
        if (accessor.isAnnotationPresent(MaxDecimalValue.class)) {
            setMaxValue(accessor.getAnnotation(MaxDecimalValue.class).value());
        }
        if (accessor.isAnnotationPresent(com.manydesigns.elements.annotations.DecimalFormat.class)) {
            DecimalFormat format =
                    new DecimalFormat(accessor.getAnnotation(
                            com.manydesigns.elements.annotations.DecimalFormat.class).value());
            setDecimalFormat(format);
        }
    }

    //--------------------------------------------------------------------------
    // Implementazione di Component
    //--------------------------------------------------------------------------
    public void readFromRequest(HttpServletRequest req) {
        if (mode == Mode.VIEW) {
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
            tmpValue = new BigDecimal(stringValue);
        } catch (NumberFormatException e) {
            //Se il testo non è un BigDecimal provo a parserizzarlo
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
        try {
            if (obj == null) {
                decimalValue = null;
            } else {
                decimalValue = (BigDecimal) accessor.get(obj);
            }
            if (decimalValue == null) {
                stringValue = null;
            } else if (decimalFormat == null) {
                stringValue = decimalValue.toString();
            } else {
                stringValue = decimalFormat.format(decimalValue);
            }
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }

    public void writeToObject(Object obj) {
        try {
            accessor.set(obj, decimalValue);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }

    //--------------------------------------------------------------------------
    // Getter/setter
    //--------------------------------------------------------------------------

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public void setMinValue(Double minValue) {
        if (minValue == null) {
            this.minValue = null;
        } else {
            this.minValue = new BigDecimal(minValue);
        }
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

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }
}
