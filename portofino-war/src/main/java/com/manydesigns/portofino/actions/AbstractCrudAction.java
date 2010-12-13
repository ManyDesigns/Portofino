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

package com.manydesigns.portofino.actions;

import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.blobs.BlobsManager;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.util.RandomUtil;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.Preparable;
import jxl.Workbook;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.apache.fop.apps.FOPException;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.lang.reflect.Method;
import java.util.Enumeration;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class  AbstractCrudAction extends PortofinoAction
        implements ServletRequestAware, Preparable, ModelDriven<CrudUnit> {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final String DEFAULT_EXPORT_FILENAME_FORMAT = "export-{0}";

    //**************************************************************************
    // ServletRequestAware implementation
    //**************************************************************************

    public HttpServletRequest req;

    public void setServletRequest(HttpServletRequest req) {
        this.req = req;
    }

    //**************************************************************************
    // ModelDriven implementation
    //**************************************************************************

    public CrudUnit getModel() {
        return rootCrudUnit;
    }

    //**************************************************************************
    // Configuration parameters and setters (for struts.xml inspections in IntelliJ)
    //**************************************************************************

    public String qualifiedName;
    public String exportFilenameFormat = DEFAULT_EXPORT_FILENAME_FORMAT;

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public void setExportFilenameFormat(String exportFilenameFormat) {
        this.exportFilenameFormat = exportFilenameFormat;
    }

    //**************************************************************************
    // Web parameters
    //**************************************************************************

    public String successReturnUrl;
    public String cancelReturnUrl;
    public String relName;
    public int selectionProviderIndex;
    public String labelSearch;
    public String code;

    //**************************************************************************
    // target identification
    //**************************************************************************

    public String targetCrudPath;
    public String targetCrudMethodName;
    public CrudUnit targetCrudUnit;

    //**************************************************************************
    // Use case instance root (tree)
    //**************************************************************************

    public CrudUnit rootCrudUnit;

    //**************************************************************************
    // Presentation/export elements
    //**************************************************************************

    public InputStream inputStream;
    public String errorMessage;
    public String contentType;
    public String fileName;
    public Long contentLength;
    public String chartId;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractCrudAction.class);

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    public String execute() throws Exception {
        if (qualifiedName == null) {
            return redirectToFirst();
        }

        Enumeration enumeration = req.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String current = (String) enumeration.nextElement();

            if (current.startsWith("crud:")) {
                String[] parts = current.split(":");
                if (parts.length != 3) {
                    continue;
                }
                targetCrudPath = parts[1];
                targetCrudMethodName = parts[2];
                if (targetCrudPath.length() == 0) {
                    targetCrudUnit = rootCrudUnit;
                } else {
                    int index = Integer.parseInt(targetCrudPath);
                    targetCrudUnit = rootCrudUnit.subCrudUnits.get(index);
                }
                Class clazz = targetCrudUnit.getClass();
                Method method = clazz.getMethod(targetCrudMethodName);
                return (String) method.invoke(targetCrudUnit);
            }
        }

        return rootCrudUnit.execute();
    }

    public abstract String redirectToFirst();

    //**************************************************************************
    // Return to search
    //**************************************************************************

    public String returnToSearch() {
        rootCrudUnit.pk = null;
        return RETURN_TO_SEARCH;
    }

    //**************************************************************************
    // Cancel
    //**************************************************************************

    public String cancel() {
        return CANCEL;
    }

    //**************************************************************************
    // Blobs
    //**************************************************************************

    public String downloadBlob() throws IOException {
        Blob blob = BlobsManager.getManager().loadBlob(code);
        contentLength = blob.getSize();
        contentType = blob.getContentType();
        inputStream = new FileInputStream(blob.getDataFile());
        fileName = blob.getFilename();
        return EXPORT;
    }

    //**************************************************************************
    // Ajax
    //**************************************************************************

    public String jsonSelectFieldOptions() {
        String text = rootCrudUnit.jsonOptions(
                relName, selectionProviderIndex, labelSearch, true);
        inputStream = new StringBufferInputStream(text);
        return JSON_SELECT_FIELD_OPTIONS;
    }

    public String jsonAutocompleteOptions() {
        String text = rootCrudUnit.jsonOptions(
                relName, selectionProviderIndex, labelSearch, false);
        inputStream = new StringBufferInputStream(text);
        return JSON_SELECT_FIELD_OPTIONS;
    }


    //**************************************************************************
    // ExportSearch
    //**************************************************************************

    public String exportSearchExcel() {
        File fileTemp = createExportTempFile();
        rootCrudUnit.exportSearchExcel(fileTemp);
        paramExport(fileTemp);
        return EXPORT;
    }

    private File createExportTempFile() {
         String exportId = RandomUtil.createRandomCode();
         return RandomUtil.getTempCodeFile(exportFilenameFormat, exportId);
     }

    private void paramExport(File fileTemp) {
        contentType = "application/ms-excel; charset=UTF-8";
        fileName = fileTemp.getName() + ".xls";

        contentLength = fileTemp.length();

        try {
            inputStream = new FileInputStream(fileTemp);
        } catch (IOException e) {
            logger.warn("IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        }
    }



    //**************************************************************************
    // ExportRead
    //**************************************************************************

    public String exportReadExcel() {
        File fileTemp = createExportTempFile();
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(fileTemp);
            rootCrudUnit.exportReadExcel(workbook);
        } catch (IOException e) {
            logger.warn("IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (RowsExceededException e) {
            logger.warn("RowsExceededException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (WriteException e) {
            logger.warn("WriteException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } finally {
            try {
                if (workbook != null)
                    workbook.close();
            }
            catch (Exception e) {
                logger.warn("IOException", e);
                SessionMessages.addErrorMessage(e.getMessage());
            }
        }        
        paramExport(fileTemp);
        return PortofinoAction.EXPORT;
    }

    //**************************************************************************
    // ExportSearchPdf
    //**************************************************************************

    public String exportSearchPdf() throws FOPException,
            IOException, TransformerException {
        File tempPdfFile = createExportTempFile();
        rootCrudUnit.exportSearchPdf(tempPdfFile);

        inputStream = new FileInputStream(tempPdfFile);

        contentType = "application/pdf";

        fileName = tempPdfFile.getName() + ".pdf";

        contentLength = tempPdfFile.length();

        return EXPORT;
    }

     //**************************************************************************
    // ExportSearchPdf
    //**************************************************************************

    public String exportReadPdf() throws FOPException,
            IOException, TransformerException {
        File tempPdfFile = createExportTempFile();
        rootCrudUnit.exportReadPdf(tempPdfFile);

        inputStream = new FileInputStream(tempPdfFile);

        contentType = "application/pdf";

        fileName = tempPdfFile.getName() + ".pdf";

        contentLength = tempPdfFile.length();

        return EXPORT;
    }

}
