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

package com.manydesigns.elements.pdf;

import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.xml.XmlBuffer;
import org.apache.commons.lang.StringUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class TableFormPdfExporter {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    private final TableForm form;
    private final Source xsltSource;
    private String title;

    public TableFormPdfExporter(TableForm form, Source xsltSource) {
        this.form = form;
        this.xsltSource = xsltSource;
    }

    public void export(OutputStream outputStream) throws FOPException,
            IOException, TransformerException {
        FopFactory fopFactory = FopFactory.newInstance();

        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, outputStream);

        // Setup XSLT
        TransformerFactory Factory = TransformerFactory.newInstance();
        Transformer transformer = Factory.newTransformer(xsltSource);

        // Set the value of a <param> in the stylesheet
        transformer.setParameter("versionParam", "2.0");

        // Setup input for XSLT transformation
        Reader reader = composeXml();
        Source src = new StreamSource(reader);

        // Resulting SAX events (the generated FO) must be piped through to
        // FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        // Start XSLT transformation and FOP processing
        transformer.transform(src, res);
        reader.close();

        outputStream.flush();
    }

    /**
     * Composes an XML document representing the current search results.
     * @return
     * @throws IOException
     */
    protected Reader composeXml() throws IOException {
        XmlBuffer xb = new XmlBuffer();
        xb.writeXmlHeader("UTF-8");
        xb.openElement("class");
        xb.openElement("table");
        if(title != null) {
            xb.write(title);
        }
        xb.closeElement("table");

        double[] columnSizes = setupColumnSizes();

        for (double columnSize : columnSizes) {
            xb.openElement("column");
            xb.openElement("width");
            xb.write(columnSize + "em");
            xb.closeElement("width");
            xb.closeElement("column");
        }

        for (TableForm.Column col : form.getColumns()) {
            xb.openElement("header");
            xb.openElement("nameColumn");
            xb.write(col.getLabel());
            xb.closeElement("nameColumn");
            xb.closeElement("header");
        }


        for (TableForm.Row row : form.getRows()) {
            xb.openElement("rows");
            for (Field field : row) {
                xb.openElement("row");
                xb.openElement("value");
                xb.write(field.getStringValue());
                xb.closeElement("value");
                xb.closeElement("row");
            }
            xb.closeElement("rows");
        }

        xb.closeElement("class");

        return new StringReader(xb.toString());
    }

    /**
     * <p>Returns an array of column sizes (in characters) for the search export.<br />
     * By default, sizes are computed comparing the relative sizes of each column,
     * consisting of the header and the values produced by the search.</p>
     * <p>Users can override this method to compute the sizes using a different algorithm,
     * or hard-coding them for a particular CRUD instance.</p>
     */
    protected double[] setupColumnSizes() {
        double[] headerSizes = new double[form.getColumns().length];
        for(int i = 0; i < headerSizes.length; i++) {
            TableForm.Column col = form.getColumns()[i];
            int length = StringUtils.length(col.getLabel());
            headerSizes[i] = length;
        }

        double[] columnSizes = new double[form.getColumns().length];
        for (TableForm.Row row : form.getRows()) {
            int i = 0;
            for (Field field : row) {
                int size = StringUtils.length(field.getStringValue());
                double relativeSize = ((double) size) / form.getRows().length;
                columnSizes[i++] += relativeSize;
            }
        }

        double totalSize = 0;
        for (int i = 0; i < columnSizes.length; i++) {
            double effectiveSize = Math.max(columnSizes[i], headerSizes[i]);
            columnSizes[i] = effectiveSize;
            totalSize += effectiveSize;
        }
        while(totalSize > 75) {
            int maxIndex = 0;
            double max = 0;
            for(int i = 0; i < columnSizes.length; i++) {
                if(columnSizes[i] > max) {
                    max = columnSizes[i];
                    maxIndex = i;
                }
            }
            columnSizes[maxIndex] -= 1;
            totalSize -= 1;
        }
        while(totalSize < 70) {
            int minIndex = 0;
            double min = Double.MAX_VALUE;
            for(int i = 0; i < columnSizes.length; i++) {
                if(columnSizes[i] < min) {
                    min = columnSizes[i];
                    minIndex = i;
                }
            }
            columnSizes[minIndex] += 1;
            totalSize += 1;
        }
        return columnSizes;
    }

    public TableForm getForm() {
        return form;
    }

    public Source getXsltSource() {
        return xsltSource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
