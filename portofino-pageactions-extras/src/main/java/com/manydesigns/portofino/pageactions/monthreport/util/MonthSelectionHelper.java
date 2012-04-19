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

package com.manydesigns.portofino.pageactions.monthreport.util;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.util.Locale;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MonthSelectionHelper {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static SelectionProvider getMonthSelectionProvider() {
        Locale locale = ElementsThreadLocals.getHttpServletRequest().getLocale();
        LocalDate localDate = new LocalDate();
        DefaultSelectionProvider monthSelectionProvider =
                new DefaultSelectionProvider("month");
        for (int i = DateTimeConstants.JANUARY; i <= DateTimeConstants.DECEMBER; i++) {
            localDate = localDate.withMonthOfYear(i);
            String label = localDate.toString("MMMMM", locale);
            monthSelectionProvider.appendRow(i, label, true);
        }
        return monthSelectionProvider;
    }


    public static SelectionProvider getYearSelectionProvider(int from, int to) {
        DefaultSelectionProvider yearSelectionProvider =
                new DefaultSelectionProvider("year");
        for (int i = from; i <= to; i++) {
            String label = Integer.toString(i);
            yearSelectionProvider.appendRow(i, label, true);
        }
        return yearSelectionProvider;
    }
}
