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

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ColumnForm extends Column {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected final Type type;
    protected final boolean inPk;

    //Known annotations
    protected Integer fieldSize;
    protected Integer maxLength;
    protected boolean multiline;
    protected boolean email;
    protected boolean cap;
    protected boolean highlightLinks;
    protected String regexp;

    protected BigDecimal minValue;
    protected BigDecimal maxValue;

    protected String dateFormat;

    public ColumnForm(Column copyFrom, PropertyAccessor columnAccessor, Type type) {
        try {
            BeanUtils.copyProperties(this, copyFrom);
        } catch (Exception e) {
            throw new Error(e);
        }
        this.type = type;
        inPk = DatabaseLogic.isInPk(copyFrom);

        FieldSize fieldSizeAnn = columnAccessor.getAnnotation(FieldSize.class);
        if(fieldSizeAnn != null) {
            fieldSize = fieldSizeAnn.value();
        }

        MaxLength maxLengthAnn = columnAccessor.getAnnotation(MaxLength.class);
        if(maxLengthAnn != null) {
            maxLength = maxLengthAnn.value();
        }

        Multiline multilineAnn = columnAccessor.getAnnotation(Multiline.class);
        if(multilineAnn != null) {
            multiline = true;
        }

        Email emailAnn = columnAccessor.getAnnotation(Email.class);
        if(emailAnn != null) {
            email = true;
        }

        CAP capAnn = columnAccessor.getAnnotation(CAP.class);
        if(capAnn != null) {
            cap = true;
        }

        HighlightLinks hlAnn = columnAccessor.getAnnotation(HighlightLinks.class);
        if(hlAnn != null) {
            highlightLinks = true;
        }

        RegExp regexpAnn = columnAccessor.getAnnotation(RegExp.class);
        if(regexpAnn != null) {
            regexp = regexpAnn.value();
        }

        MinDecimalValue minDecimalValueAnn = columnAccessor.getAnnotation(MinDecimalValue.class);
        if(minDecimalValueAnn != null) {
            minValue = new BigDecimal(minDecimalValueAnn.value());
        } else {
            MinIntValue minIntValueAnn = columnAccessor.getAnnotation(MinIntValue.class);
            if(minIntValueAnn != null) {
                minValue = new BigDecimal(minIntValueAnn.value());
            }
        }

        MaxDecimalValue maxDecimalValueAnn = columnAccessor.getAnnotation(MaxDecimalValue.class);
        if(maxDecimalValueAnn != null) {
            maxValue = new BigDecimal(maxDecimalValueAnn.value());
        } else {
            MaxIntValue maxIntValueAnn = columnAccessor.getAnnotation(MaxIntValue.class);
            if(maxIntValueAnn != null) {
                maxValue = new BigDecimal(maxIntValueAnn.value());
            }
        }

        DateFormat dateFormatAnn = columnAccessor.getAnnotation(DateFormat.class);
        if(dateFormatAnn != null) {
            dateFormat = dateFormatAnn.value();
        }
    }

    public void copyTo(Column column) {
        column.setJavaType(getJavaType());
        column.setPropertyName(StringUtils.defaultIfEmpty(getPropertyName(), null));

        //Annotations
        column.getAnnotations().clear();
        if(fieldSize != null) {
            Annotation ann = new Annotation(column, FieldSize.class.getName());
            ann.getValues().add(fieldSize.toString());
            column.getAnnotations().add(ann);
        }
        if(maxLength != null) {
            Annotation ann = new Annotation(column, MaxLength.class.getName());
            ann.getValues().add(maxLength.toString());
            column.getAnnotations().add(ann);
        }
        if(multiline) {
            Annotation ann = new Annotation(column, Multiline.class.getName());
            ann.getValues().add("true");
            column.getAnnotations().add(ann);
        }
        if(email) {
            Annotation ann = new Annotation(column, Email.class.getName());
            ann.getValues().add("true");
            column.getAnnotations().add(ann);
        }
        if(cap) {
            Annotation ann = new Annotation(column, CAP.class.getName());
            ann.getValues().add("true");
            column.getAnnotations().add(ann);
        }
        if(highlightLinks) {
            Annotation ann = new Annotation(column, HighlightLinks.class.getName());
            ann.getValues().add("true");
            column.getAnnotations().add(ann);
        }
        if(!StringUtils.isEmpty(regexp)) {
            Annotation ann = new Annotation(column, RegExp.class.getName());
            ann.getValues().add(regexp);
            ann.getValues().add("elements.error.field.regexp.format"); //Default error message
            column.getAnnotations().add(ann);
        }
        if(minValue != null) {
            Annotation ann = new Annotation(column, MinDecimalValue.class.getName());
            ann.getValues().add(minValue.toString());
            column.getAnnotations().add(ann);
        }
        if(maxValue != null) {
            Annotation ann = new Annotation(column, MaxDecimalValue.class.getName());
            ann.getValues().add(maxValue.toString());
            column.getAnnotations().add(ann);
        }
        if(!StringUtils.isEmpty(dateFormat)) {
            Annotation ann = new Annotation(column, DateFormat.class.getName());
            ann.getValues().add(dateFormat);
            column.getAnnotations().add(ann);
        }
    }

    @Override
    @Updatable(false)
    @Insertable(false)
    @Label("Name")
    public String getColumnName() {
        return super.getColumnName();
    }

    @Override
    @FieldSize(4)
    @Updatable(false)
    @Insertable(false)
    public Integer getLength() {
        return super.getLength();
    }

    @Override
    @FieldSize(4)
    @Updatable(false)
    @Insertable(false)
    public Integer getScale() {
        return super.getScale();
    }

    @Label("Type")
    @Updatable(false)
    @Insertable(false)
    public Type getType() {
        return type;
    }

    @Override
    @Label("Class")
    @Select(nullOption = false)
    public String getJavaType() {
        return super.getJavaType();
    }

    @Override
    public boolean isNullable() {
        return super.isNullable();
    }

    @Label("In primary key")
    @Updatable(false)
    @Insertable(false)
    public boolean isInPk() {
        return inPk;
    }

    public Integer getFieldSize() {
        return fieldSize;
    }

    public void setFieldSize(Integer fieldSize) {
        this.fieldSize = fieldSize;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public boolean isEmail() {
        return email;
    }

    public void setEmail(boolean email) {
        this.email = email;
    }

    public boolean isCap() {
        return cap;
    }

    public void setCap(boolean cap) {
        this.cap = cap;
    }

    public boolean isHighlightLinks() {
        return highlightLinks;
    }

    public void setHighlightLinks(boolean highlightLinks) {
        this.highlightLinks = highlightLinks;
    }

    @FieldSize(75)
    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    @PrecisionScale(scale = 10, precision = 100)
    @DecimalFormat("#.#####")
    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    @PrecisionScale(scale = 10, precision = 100)
    @DecimalFormat("#.#####")
    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}
