/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.xls;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.fields.*;
import jxl.CellView;
import jxl.write.*;
import jxl.write.Number;

import java.lang.Boolean;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import static com.manydesigns.elements.fields.BooleanField.FALSE_LABEL_I18N;
import static com.manydesigns.elements.fields.BooleanField.TRUE_LABEL_I18N;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 */
public class XlsUtil {
  public static final String copyright =
    "Copyright (C) 2005-2021 ManyDesigns srl";

  //Map for formatters, only 350 availables in jxl
  private HashMap<String, WritableCellFormat> formatMap = new HashMap<String, WritableCellFormat>();

  public  void addFieldToCell(WritableSheet sheet, int i, int j, Field field) throws WriteException {
    if (field instanceof NumericField) {
      NumericField numField = (NumericField) field;
      if (numField.getValue() != null) {
        jxl.write.Number number;
        BigDecimal decimalValue = numField.getValue();
        if (numField.getDecimalFormat() == null) {
          number = new Number(j, i, decimalValue == null ? null : decimalValue.doubleValue());
        } else {
          number = new Number(j, i, decimalValue == null ? null : decimalValue.doubleValue(),
            getNumberFormat(numField.getDecimalFormat().toPattern()));
        }
        sheet.addCell(number);
      }
    } else if (field instanceof PasswordField) {
      Label label = new Label(j, i, PasswordField.PASSWORD_PLACEHOLDER);
      sheet.addCell(label);
    } else if (field instanceof DateField) {
      DateField dateField = (DateField) field;
      DateTime dateCell;
      Date date = dateField.getValue();
      if (date != null) {
        dateCell = new DateTime(j, i, date, getDateFormat(dateField.getDatePattern()));
        sheet.addCell(dateCell);
      }
    }else if (field instanceof BooleanField) {
      BooleanField booleanField = (BooleanField) field;
      Boolean booleanFieldValue = booleanField.getValue();
      if (booleanFieldValue != null) {
        Label label = new Label(j, i, booleanFieldValue? getText(TRUE_LABEL_I18N):getText(FALSE_LABEL_I18N));
        sheet.addCell(label);
      }
    } else {
      Label label = new Label(j, i, field.getStringValue());
      sheet.addCell(label);
    }
  }

  protected String getText(String key, Object... args) {
    return ElementsThreadLocals.getTextProvider().getText(key, args);
  }

  public static void autoSizeColumns(WritableSheet sheet, int columns) {
    for (int c = 0; c < columns; c++) {
      CellView cell = sheet.getColumnView(c);
      cell.setAutosize(true);
      sheet.setColumnView(c, cell);
    }
  }

  private  WritableCellFormat getDateFormat(String pattern) {
    if (formatMap.containsKey(pattern)) {
      return formatMap.get(pattern);
    }
    DateFormat dateFormat = new DateFormat(pattern);
    WritableCellFormat wDateFormat = new WritableCellFormat(dateFormat);

    formatMap.put(pattern, wDateFormat);
    return wDateFormat;
  }

  private  WritableCellFormat getNumberFormat(String pattern) {
    if (formatMap.containsKey(pattern)) {
      return formatMap.get(pattern);
    }
    NumberFormat numberFormat = new NumberFormat(pattern);
    WritableCellFormat wNumberFormat = new WritableCellFormat(numberFormat);

    formatMap.put(pattern, wNumberFormat);
    return wNumberFormat;
  }
}
