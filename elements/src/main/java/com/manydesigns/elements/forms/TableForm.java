/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.forms;

import com.manydesigns.elements.Element;
import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.annotations.Help;
import com.manydesigns.elements.composites.AbstractCompositeElement;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.FieldUtils;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class TableForm implements Element {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    protected String selectInputName = "select";

    protected final Column[] columns;
    protected final Row[] rows;

    protected String prefix;

    protected String caption;
    protected boolean selectable = false;
    protected TextFormat keyTextFormat;

    protected boolean condensed = false;
    protected boolean striped = true;

    private static final String SELECTION_CELL_CLASS = "selection-cell";

    //**************************************************************************
    // Costruttori
    //**************************************************************************

    public TableForm(int nRows, PropertyAccessor... propertyAccessors) {
        rows = new Row[nRows];
        columns = new Column[propertyAccessors.length];

        for (int i = 0; i < nRows; i++) {
            rows[i] = new Row(i);
        }

        for (int i = 0; i < columns.length; i++) {
            columns[i] = new Column(propertyAccessors[i]);
        }
    }

    //**************************************************************************
    // Implementazione di Element
    //**************************************************************************

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.openElement("table");
        xb.addAttribute("class",
                "table mde-table-form" +
                (condensed ? " table-condensed" : "") +
                (striped ? " table-striped" : ""));
        if (caption != null) {
            xb.writeCaption(caption);
        }
        xb.openElement("thead");
        xb.openElement("tr");

        if (selectable) {
            xb.openElement("th");
            xb.openElement("div");
            xb.addAttribute("class", " squared-dark ");
            xb.openElement("input");
            xb.addAttribute("type", "checkbox");
            xb.addAttribute("title", "select-all");

            String id =  RandomUtil.createRandomId(10) ;
            xb.addAttribute("id", id );
            String js =
                    "$(this).closest('table').find('div." + SELECTION_CELL_CLASS + " input').prop('checked', $(this).prop('checked'));";
            xb.addAttribute("onchange", js);
            xb.closeElement("input");

            xb.openElement("label");
            xb.addAttribute("for", id );
            xb.closeElement("label");
            xb.closeElement("div");
            xb.closeElement("th");
        }

        for (Column column : columns) {
            xb.openElement("th");
            if (column.title != null) {
                xb.addAttribute("title", column.title);
            }
            PropertyAccessor property = column.getPropertyAccessor();
            xb.addAttribute("data-property-name", property.getName());
            xb.openElement("p");
            xb.addAttribute("class", "form-control-static");
            if(column.headerTextFormat != null) {
                Map<String, Object> formatParameters = new HashMap<String, Object>();
                formatParameters.put("label", StringEscapeUtils.escapeHtml(column.getActualLabel()));
                formatParameters.put("property", property);
                xb.writeNoHtmlEscape(column.headerTextFormat.format(formatParameters));
            } else {
                xb.write(column.getActualLabel());
            }
            xb.closeElement("p");
            xb.closeElement("th");
        }
        xb.closeElement("tr");
        xb.closeElement("thead");

        if(rows.length > 0) {
            xb.openElement("tbody");
            for (Row row : rows) {
                row.toXhtml(xb);
            }
            xb.closeElement("tbody");
        }

        xb.closeElement("table");
    }

    public void readFromRequest(HttpServletRequest req) {
        for (Row row : rows) {
            row.readFromRequest(req);
        }
    }

    public boolean validate() {
        boolean result = true;
        for (Row row : rows) {
            result = row.validate() && result;
        }
        return result;
    }

    public void readFromObject(Object obj) {
        Class clazz = obj.getClass();
        if (clazz.isArray()) { // Tratta obj come un array
            // Scorre tutti gli ellementi dell'array obj,
            // indipendentemente da quante righe ci sono nell table form.
            // Eventualmente lancia Eccezione.
            final int arrayLength = Array.getLength(obj);
            for (int i = 0; i < arrayLength; i++) {
                Object currentObj = Array.get(obj, i);
                rows[i].readFromObject(currentObj);
            }

            // Scorre le rimanenti righe del table form,
            // passano null come ottetto di bind.
            for (int i = arrayLength; i < rows.length; i++) {
                rows[i].readFromObject(null);
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            // Tratta obj come collection
            Collection collection = (Collection)obj;

            int i = 0;
            for (Object currentObj : collection) {
                rows[i].readFromObject(currentObj);
                i++;
            }

            for (; i < rows.length; i++) {
                rows[i].readFromObject(null);
            }
        }
    }

    public void writeToObject(Object obj) {
        Class clazz = obj.getClass();
        if (clazz.isArray()) { // Tratta obj come un array
            // Scorre tutti gli elementi dell'array obj,
            // indipendentemente da quante righe ci sono nel table form.
            // Eventualmente lancia Eccezione.
            final int arrayLength = Array.getLength(obj);
            for (int i = 0; i < arrayLength; i++) {
                Object currentObj = Array.get(obj, i);
                rows[i].writeToObject(currentObj);
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            // Tratta obj come collection
            Collection collection = (Collection)obj;

            int i = 0;
            for (Object currentObj : collection) {
                rows[i].writeToObject(currentObj);
                i++;
            }
        }
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public TextFormat getKeyGenerator() {
        return keyTextFormat;
    }

    public void setKeyGenerator(TextFormat keyTextFormat) {
        this.keyTextFormat = keyTextFormat;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public Column[] getColumns() {
        return columns;
    }

    public Row[] getRows() {
        return rows;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * If a table is "condensed", it is rendered with less whitespace to make it more compact.
     * @return the value of the "condensed" property.
     */
    public boolean isCondensed() {
        return condensed;
    }

    public void setCondensed(boolean condensed) {
        this.condensed = condensed;
    }

    /**
     * If a table is "striped", it is rendered with rows of alternating background colors.
     * @return the value of the "striped" property.
     */
    public boolean isStriped() {
        return striped;
    }

    public void setStriped(boolean striped) {
        this.striped = striped;
    }

    //**************************************************************************
    // Inner class: Row
    //**************************************************************************

    public class Row extends AbstractCompositeElement<Field> {
        public static final String copyright =
                "Copyright (c) 2005-2015, ManyDesigns srl";

        protected String key;
        protected final int index;

        public Row(int index) {
            super(columns.length);
            this.index = index;
        }

        public void toXhtml(@NotNull XhtmlBuffer xb) {
            xb.openElement("tr");

            if (selectable) {
                String[] inputNameArgs = {prefix, "selection"};
                String id = RandomUtil.createRandomId(10)+index;
                String selection = StringUtils.join(inputNameArgs);
                xb.openElement("td");
                xb.openElement("div");
                xb.addAttribute("class", SELECTION_CELL_CLASS+" squared-light ");
                xb.writeInputCheckbox(id, selection, key, false, false, null, String.valueOf(index));

                xb.openElement("label");
                xb.addAttribute("for", id);
                xb.closeElement("label");
                xb.closeElement("div");
                xb.closeElement("td");
            }

            for (Field current : this) {
                xb.openElement("td");
                if (!current.getErrors().isEmpty()) {
                    xb.addAttribute("class", "has-error");
                }
                current.valueToXhtml(xb);
                current.errorsToXhtml(xb);
                xb.closeElement("td");
            }

            xb.closeElement("tr");
        }

        public void readFromRequest(HttpServletRequest req) {
            for (Field current : this) {
                current.readFromRequest(req);
            }
        }

        public boolean validate() {
            boolean result = true;
            for (Field current : this) {
                result = current.validate() && result;
            }
            return result;
        }

        public void readFromObject(Object obj) {
            if (keyTextFormat == null) {
                key = Integer.toString(index);
            } else {
                key = keyTextFormat.format(obj);
            }
            int index = 0;
            for (Field field : this) {
                Column column = columns[index];
                TextFormat hrefTextFormat = column.getHrefTextFormat();
                TextFormat titleTextFormat = column.getTitleTextFormat();
                if (hrefTextFormat != null) {
                    field.setHref(hrefTextFormat.format(obj));
                    if (titleTextFormat != null) {
                        field.setTitle(titleTextFormat.format(obj));
                    }
                }
                field.readFromObject(obj);
                index++;
            }
        }

        public void writeToObject(Object obj) {
            for (Field current : this) {
                current.writeToObject(obj);
            }
        }

        public String getKey() {
            return key;
        }

        public int getIndex() {
            return index;
        }
    }

    //**************************************************************************
    // Inner class: Column
    //**************************************************************************

    public class Column {
        public static final String copyright =
                "Copyright (c) 2005-2015, ManyDesigns srl";

        protected final PropertyAccessor propertyAccessor;

        protected String label;
        protected String title;
        protected TextFormat headerTextFormat;
        protected TextFormat hrefTextFormat;
        protected TextFormat titleTextFormat;

        //**************************************************************************
        // Costruttori
        //**************************************************************************

        public Column(PropertyAccessor propertyAccessor) {
            this.propertyAccessor = propertyAccessor;

            label = FieldUtils.getLabel(propertyAccessor);

            if (propertyAccessor.isAnnotationPresent(Help.class)) {
                title = propertyAccessor.getAnnotation(Help.class).value();
            }
        }

        //**************************************************************************
        // Getter/setter
        //**************************************************************************

        public PropertyAccessor getPropertyAccessor() {
            return propertyAccessor;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getActualLabel() {
            boolean capitalize = ElementsProperties.getConfiguration().getBoolean(
                    ElementsProperties.FIELDS_LABEL_CAPITALIZE);
            if (capitalize) {
                return StringUtils.capitalize(label);
            } else {
                return label;
            }
        }

        public TextFormat getHeaderTextFormat() {
            return headerTextFormat;
        }

        public void setHeaderTextFormat(TextFormat headerTextFormat) {
            this.headerTextFormat = headerTextFormat;
        }

        public TextFormat getHrefTextFormat() {
            return hrefTextFormat;
        }

        public void setHrefTextFormat(TextFormat hrefTextFormat) {
            this.hrefTextFormat = hrefTextFormat;
        }

        public TextFormat getTitleTextFormat() {
            return titleTextFormat;
        }

        public void setTitleTextFormat(TextFormat altTextFormat) {
            this.titleTextFormat = altTextFormat;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}