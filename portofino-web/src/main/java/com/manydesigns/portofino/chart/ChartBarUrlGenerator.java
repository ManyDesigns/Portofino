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
