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

package com.manydesigns.portofino.actions.admin.tables.forms;

import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ColumnForm extends Column {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final Type type;
    protected final boolean inPk;

    //Known annotations
    protected Integer fieldSize;
    protected Integer maxLength;
    protected String typeOfContent;
    protected String stringFormat;
    protected boolean highlightLinks;
    protected String regexp;
    protected boolean fileBlob;

    protected BigDecimal minValue;
    protected BigDecimal maxValue;
    protected String decimalFormat;
    //TODO PrecisionScale (non gestita a livello di field)

    protected String dateFormat;

    public static final String[] KNOWN_ANNOTATIONS = {
            FieldSize.class.getName(), MaxLength.class.getName(), Multiline.class.getName(), RichText.class.getName(),
            Email.class.getName(), CAP.class.getName(), CodiceFiscale.class.getName(), PartitaIva.class.getName(),
            Password.class.getName(), Phone.class.getName(),
            HighlightLinks.class.getName(), RegExp.class.getName(), FileBlob.class.getName(),
            MinDecimalValue.class.getName(), MinIntValue.class.getName(), MaxDecimalValue.class.getName(),
            MaxIntValue.class.getName(), DecimalFormat.class.getName(), DateFormat.class.getName()
    };

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
        if(multilineAnn != null && multilineAnn.value()) {
            typeOfContent = Multiline.class.getName();
        }
        RichText richTextAnn = columnAccessor.getAnnotation(RichText.class);
        if(richTextAnn != null && richTextAnn.value()) {
            typeOfContent = RichText.class.getName();
        }

        if(columnAccessor.isAnnotationPresent(Email.class)) {
            stringFormat = Email.class.getName();
        }
        if(columnAccessor.isAnnotationPresent(CAP.class)) {
            stringFormat = CAP.class.getName();
        }
        if(columnAccessor.isAnnotationPresent(CodiceFiscale.class)) {
            stringFormat = CodiceFiscale.class.getName();
        }
        if(columnAccessor.isAnnotationPresent(PartitaIva.class)) {
            stringFormat = PartitaIva.class.getName();
        }
        if(columnAccessor.isAnnotationPresent(Password.class)) {
            stringFormat = Password.class.getName();
        }
        if(columnAccessor.isAnnotationPresent(Phone.class)) {
            stringFormat = Phone.class.getName();
        }

        HighlightLinks hlAnn = columnAccessor.getAnnotation(HighlightLinks.class);
        if(hlAnn != null) {
            highlightLinks = hlAnn.value();
        }

        RegExp regexpAnn = columnAccessor.getAnnotation(RegExp.class);
        if(regexpAnn != null) {
            regexp = regexpAnn.value();
        }

        FileBlob fileBlobAnn = columnAccessor.getAnnotation(FileBlob.class);
        if(fileBlobAnn != null) {
            fileBlob = true;
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

        DecimalFormat decimalFormatAnn = columnAccessor.getAnnotation(DecimalFormat.class);
        if(decimalFormatAnn != null) {
            decimalFormat = decimalFormatAnn.value();
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
        for(String annotationClass : KNOWN_ANNOTATIONS) {
            removeAnnotation(annotationClass, column.getAnnotations());
        }

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
        if(typeOfContent != null) {
            Annotation ann = new Annotation(column, typeOfContent);
            ann.getValues().add("true");
            column.getAnnotations().add(ann);
        }
        if(stringFormat != null) {
            Annotation ann = new Annotation(column, stringFormat);
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
        if(fileBlob) {
            Annotation ann = new Annotation(column, FileBlob.class.getName());
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
        if(!StringUtils.isEmpty(decimalFormat)) {
            Annotation ann = new Annotation(column, DecimalFormat.class.getName());
            ann.getValues().add(decimalFormat);
            column.getAnnotations().add(ann);
        }
        if(!StringUtils.isEmpty(dateFormat)) {
            Annotation ann = new Annotation(column, DateFormat.class.getName());
            ann.getValues().add(dateFormat);
            column.getAnnotations().add(ann);
        }
    }

    protected void removeAnnotation(String annotationClass, List<Annotation> annotations) {
        Iterator<Annotation> it = annotations.iterator();
        while (it.hasNext()) {
            Annotation ann = it.next();
            if(ann.getType().equals(annotationClass)) {
                it.remove();
            }
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

    @Updatable(false)
    @Insertable(false)
    @Label("Length")
    public String getShortLength() {
        if(getLength() == null) {
            return null;
        }
        String[] suffix = new String[] { "", "K", "M", "G", "T" };
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("##0E0");
        String result = decimalFormat.format(getLength());
        int suffixIndex = Character.getNumericValue(result.charAt(result.length() - 1)) / 3;
        return result.replaceAll("E[0-9]", suffix[suffixIndex]);
    }

    @Override
    @FieldSize(4)
    @Updatable(false)
    @Insertable(false)
    public Integer getScale() {
        return super.getScale();
    }

    @Override
    @RegExp(
        value = "(_|$|[a-z]|[A-Z]|[\u0080-\ufffe])(_|$|[a-z]|[A-Z]|[\u0080-\ufffe]|[0-9])*",
        errorMessage = "invalid.property.name")
    public String getPropertyName() {
        return super.getPropertyName();
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

    //Work around Introspector bug (boolean property descriptors not inherited)

    @Label("Null")
    @Insertable(false)
    @Updatable(false)
    public boolean isReallyNullable() {
        return isNullable();
    }

    @Label("Autoincrement")
    @Insertable(false)
    @Updatable(false)
    public boolean isReallyAutoincrement() {
        return isAutoincrement();
    }

    @Label("In primary key")
    @Updatable(false)
    @Insertable(false)
    public boolean isInPk() {
        return inPk;
    }

    @MinIntValue(1)
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

    public String getTypeOfContent() {
        return typeOfContent;
    }

    public void setTypeOfContent(String typeOfContent) {
        this.typeOfContent = typeOfContent;
    }

    public String getStringFormat() {
        return stringFormat;
    }

    public void setStringFormat(String stringFormat) {
        this.stringFormat = stringFormat;
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

    public boolean isFileBlob() {
        return fileBlob;
    }

    public void setFileBlob(boolean fileBlob) {
        this.fileBlob = fileBlob;
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

    public String getDecimalFormat() {
        return decimalFormat;
    }

    public void setDecimalFormat(String decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}
