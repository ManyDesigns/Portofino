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

package com.manydesigns.portofino.chart;

import com.manydesigns.elements.text.OgnlTextFormat;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

public class ChartBarUrlGenerator implements CategoryURLGenerator {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected final OgnlTextFormat format;
    protected final BarURLGeneratorValue value;

    public ChartBarUrlGenerator(String expression) {
        format = OgnlTextFormat.create(expression);
        format.setUrl(true);
        value = new BarURLGeneratorValue();
    }

    public String generateURL(CategoryDataset dataset, int series, int category) {
        ComparableWrapper c1 = (ComparableWrapper) dataset.getRowKey(series);
        ComparableWrapper c2 = (ComparableWrapper) dataset.getColumnKey(category);
        value.dataset = dataset;
        value.series = c1.getObject();
        value.category = c2.getObject();
        return format.format(value);
    }

    static class BarURLGeneratorValue {
        public CategoryDataset dataset;
        public Comparable series;
        public Comparable category;
    }
}
