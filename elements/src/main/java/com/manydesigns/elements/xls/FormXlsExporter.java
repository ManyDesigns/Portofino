/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.forms.FieldSet;
import com.manydesigns.elements.forms.Form;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.*;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class FormXlsExporter {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    private final Form form;
    private boolean useTemporaryFileDuringWrite = false;
    private String title = " ";

    public FormXlsExporter(Form form) {
        this.form = form;
    }

    public void export(OutputStream outputStream)
            throws IOException, WriteException {
        WritableWorkbook workbook;
        WorkbookSettings workbookSettings = new WorkbookSettings();
        workbookSettings.setUseTemporaryFileDuringWrite(useTemporaryFileDuringWrite);
        workbook = Workbook.createWorkbook(outputStream, workbookSettings);
        WritableSheet sheet = workbook.createSheet(title, workbook.getNumberOfSheets());

        addHeaderToSheet(sheet);

        int i = 1;
        for (FieldSet fieldset : form) {
            int j = 0;
            for (Field field : fieldset.fields()) {
                XlsUtil.addFieldToCell(sheet, i, j, field);
                j++;
            }
            i++;
        }
        workbook.write();
        workbook.close();
        outputStream.flush();
    }

    private WritableCellFormat headerExcel() {
        WritableFont fontCell = new WritableFont(WritableFont.ARIAL, 12,
             WritableFont.BOLD, true);
        return new WritableCellFormat (fontCell);
    }

    private void addHeaderToSheet(WritableSheet sheet) throws WriteException {
        WritableCellFormat formatCell = headerExcel();
        int i = 0;
        for (FieldSet fieldset : form) {
            for (Field field : fieldset.fields()) {
                sheet.addCell(new jxl.write.Label(i, 0, field.getLabel(), formatCell));
                i++;
            }
        }
    }

    public Form getForm() {
        return form;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isUseTemporaryFileDuringWrite() {
        return useTemporaryFileDuringWrite;
    }

    public void setUseTemporaryFileDuringWrite(boolean useTemporaryFileDuringWrite) {
        this.useTemporaryFileDuringWrite = useTemporaryFileDuringWrite;
    }
}
