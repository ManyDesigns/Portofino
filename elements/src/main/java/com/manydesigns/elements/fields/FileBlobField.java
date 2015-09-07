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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.MaxLength;
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.MemoryUtil;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.xml.XhtmlBuffer;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.controller.StripesRequestWrapper;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.Callable;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class FileBlobField extends AbstractField<Blob> implements MultipartRequestField<Blob> {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String UPLOAD_KEEP = "_keep";
    public static final String UPLOAD_MODIFY = "_modify";
    public static final String UPLOAD_DELETE = "_delete";

    public static final String OPERATION_SUFFIX = "_operation";
    public static final String CODE_SUFFIX = "_code";
    public static final String INNER_SUFFIX = "_inner";

    private final int size;
    public final Callable<String> DEFAULT_CODE_GENERATOR = new Callable<String>() {
        @Override
        public String call() {
            return RandomUtil.createRandomId(size);
        }
    };

    protected String innerId;
    protected String operationInputName;
    protected String codeInputName;

    protected Blob blob;
    protected String blobError;
    protected Callable<String> blobCodeGenerator = DEFAULT_CODE_GENERATOR;

    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public FileBlobField(PropertyAccessor accessor, Mode mode) {
        this(accessor, mode, null);
    }

    public FileBlobField(@NotNull PropertyAccessor accessor,
                         @NotNull Mode mode,
                         @Nullable String prefix) {
        super(accessor, mode, prefix);

        innerId = id + INNER_SUFFIX;
        operationInputName = inputName + OPERATION_SUFFIX;
        codeInputName = inputName + CODE_SUFFIX;

        if (accessor.isAnnotationPresent(MaxLength.class)) {
            size = Math.min(25, accessor.getAnnotation(MaxLength.class).value());
        } else {
            size = 25;
        }
    }

    //**************************************************************************
    // AbstractField implementation
    //**************************************************************************
    public void valueToXhtml(XhtmlBuffer xb) {
        if (mode.isView(insertable, updatable)) {
            valueToXhtmlView(xb);
        } else if (mode.isEdit()) {
            valueToXhtmlEdit(xb);
        } else if (mode.isPreview()) {
            valueToXhtmlPreview(xb);
        } else if (mode.isHidden()) {
            valueToXhtmlHidden(xb);
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    public String getStringValue() {
        if (blob == null) {
            return null;
        } else {
            return blob.getFilename();
        }
    }

    @Override
    public void setStringValue(String stringValue) {
        if(blob != null) {
            blob.setFilename(stringValue);
        }
    }

    public void valueToXhtmlPreview(XhtmlBuffer xb) {
        valueToXhtmlView(xb);
        valueToXhtmlHidden(xb);
    }

    private void valueToXhtmlHidden(XhtmlBuffer xb) {
        xb.writeInputHidden(operationInputName, UPLOAD_KEEP);
        if (blob != null) {
            xb.writeInputHidden(codeInputName, blob.getCode());
        }
    }

    public void valueToXhtmlView(XhtmlBuffer xb) {
        xb.openElement("p");
        xb.addAttribute("id", id);
        xb.addAttribute("class", STATIC_VALUE_CSS_CLASS);
        if (blobError != null) {
            xb.openElement("div");
            xb.addAttribute("class", "blob-error");
            xb.write(blobError);
            xb.closeElement("div");
        } else if (blob != null) {
            writeBlobFilenameAndSize(xb);
        }
        xb.closeElement("p");
    }

    public void writeBlobFilenameAndSize(XhtmlBuffer xb) {
        if (href != null) {
            xb.openElement("a");
            xb.addAttribute("href", href);
        }
        xb.write(blob.getFilename());
        if (href != null) {
            xb.closeElement("a");
        }
        xb.write(" (");
        xb.write(MemoryUtil.bytesToHumanString(blob.getSize()));
        xb.write(")");
    }

    private void valueToXhtmlEdit(XhtmlBuffer xb) {
        if (blob == null || blobError != null) { //TODO if there is an error, you cannot remove the old, wrong blob value without first uploading a new file
            xb.writeInputHidden(operationInputName, UPLOAD_MODIFY);
            xb.writeInputFile(id, inputName, false);
        } else {
            xb.openElement("p");
            xb.addAttribute("class", STATIC_VALUE_CSS_CLASS);
            xb.addAttribute("id", id);
            writeBlobFilenameAndSize(xb);
            xb.closeElement("p");

            /*xb.openElement("div");
            writeBlobFilenameAndSize(xb);
            xb.closeElement("div");*/

            xb.openElement("div");
            xb.addAttribute("class", "radio radio-inline");

            String radioId = id + UPLOAD_KEEP;
            String script = "var inptxt = this.ownerDocument.getElementById('"
                    + StringEscapeUtils.escapeJavaScript(innerId) + "');"
                    + "inptxt.disabled=true;inptxt.value='';";
            script+="$('#"+StringEscapeUtils.escapeJavaScript(innerId)+"').fileinput('disable');";
             script+="$('#fileinput_"+StringEscapeUtils.escapeJavaScript(id)+"').hide();";

            printRadio(xb, radioId, "elements.field.upload.keep",
                    UPLOAD_KEEP, true, script);

            radioId = id + UPLOAD_MODIFY;
            script = "var inptxt = this.ownerDocument.getElementById('"
                    + StringEscapeUtils.escapeJavaScript(innerId) + "');"
                    + "inptxt.disabled=false;inptxt.value='';";

            script+="$('#"+StringEscapeUtils.escapeJavaScript(innerId)+"').fileinput('enable');";
             script+="$('#fileinput_"+StringEscapeUtils.escapeJavaScript(id)+"').show();";

            printRadio(xb, radioId, "elements.field.upload.update",UPLOAD_MODIFY, false, script);

            if(!isRequired()) {
                radioId = id + UPLOAD_DELETE;
                script = "var inptxt = this.ownerDocument.getElementById('"
                        + StringEscapeUtils.escapeJavaScript(innerId) + "');"
                        + "inptxt.disabled=true;inptxt.value='';";
                script+="$('#"+StringEscapeUtils.escapeJavaScript(innerId)+"').fileinput('disable');";
                script+="$('#fileinput_"+StringEscapeUtils.escapeJavaScript(id)+"').hide();";
                printRadio(xb, radioId, "elements.field.upload.delete",
                        UPLOAD_DELETE, false, script);
            }

            xb.closeElement("div");

            xb.openElement("div");
            xb.addAttribute("class", "fileinput");
            xb.addAttribute("style", "display:none");
            xb.addAttribute("id","fileinput_"+id);
            xb.writeInputFile(innerId, inputName , true);
            xb.writeInputHidden(codeInputName, blob.getCode());
            xb.closeElement("div");

            /*xb.closeElement("p"); */
        }
    }

    protected void printRadio(XhtmlBuffer xb, String radioId, String labelKey,
                              String value, boolean checked, String script) {
        xb.writeInputRadio(radioId, operationInputName, value,
                checked, false, script);

        xb.openElement("label");
        xb.addAttribute("for", radioId);
        //xb.addAttribute("class", "radio");
        xb.write(getText(labelKey));
        xb.closeElement("label");
    }

    //**************************************************************************
    // Element implementation
    //**************************************************************************
    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);
        if (mode.isView(insertable, updatable)) {
            return;
        }
        String updateTypeStr = req.getParameter(operationInputName);
        if (UPLOAD_MODIFY.equals(updateTypeStr)) {
            saveUpload(req);
        } else if (UPLOAD_DELETE.equals(updateTypeStr)) {
            forgetBlob();
        } else {
            // in all other cases (updateTypeStr is UPLOAD_KEEP,
            // null, or other values) keep the existing blob
            keepOldBlob(req);
        }
    }

    protected void forgetBlob() {
        blob = null;
    }

    protected void keepOldBlob(HttpServletRequest req) {
        String code = req.getParameter(codeInputName);
        if(!StringUtils.isBlank(code)) {
            blob = new Blob(code);
        }
    }

    protected void saveUpload(HttpServletRequest req) {
        StripesRequestWrapper stripesRequest =
            StripesRequestWrapper.findStripesWrapper(req);
        final FileBean fileBean = stripesRequest.getFileParameterValue(inputName);

        if (fileBean != null) {
            blob = new Blob(generateNewCode()) {
                @Override
                public void dispose() {
                    super.dispose();
                    try {
                        fileBean.delete();
                    } catch (IOException e) {
                        logger.warn("Could not delete file bean", e);
                    }
                }
            };
            try {
                blob.setInputStream(fileBean.getInputStream());
                blob.setFilename(fileBean.getFileName());
                blob.setContentType(fileBean.getContentType());
                blob.setPropertiesLoaded(true);
            } catch (IOException e) {
                logger.error("Could not read upload", e);
                forgetBlob();
                blobError = getText("elements.error.field.fileblob.uploadFailed");
                try {
                    fileBean.delete();
                } catch (IOException e1) {
                    logger.error("Could not delete FileBean", e1);
                }
            }
        } else {
            logger.debug("An update of a blob was requested, but nothing was uploaded. The previous value will be kept.");
            keepOldBlob(req);
        }
    }

    protected String generateNewCode() {
        try {
            return blobCodeGenerator.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validate() {
        if (mode.isView(insertable, updatable) || (mode.isBulk() && !bulkChecked)) {
            return true;
        }

        boolean result = true;
        if (required && (blob == null)) {
            errors.add(getText("elements.error.field.required"));
            result = false;
        }
        return result;
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        if (obj == null) {
            forgetBlob();
        } else {
            String code  = (String) accessor.get(obj);
            if(StringUtils.isBlank(code)) {
                forgetBlob();
            } else {
                blob = new Blob(code);
            }
        }
    }

    public void writeToObject(Object obj) {
        if (blob == null) {
            writeToObject(obj, null);
        } else {
            writeToObject(obj, blob.getCode());
        }
    }

    public Blob getValue() {
        return blob;
    }

    public void setValue(Blob blob) {
        this.blob = blob;
    }

    public String getCodeInputName() {
        return codeInputName;
    }

    public String getOperationInputName() {
        return operationInputName;
    }

    public void setOperationInputName(String operationInputName) {
        this.operationInputName = operationInputName;
    }

    public String getBlobError() {
        return blobError;
    }

    public void setBlobError(String blobError) {
        this.blobError = blobError;
    }

    public Callable<String> getBlobCodeGenerator() {
        return blobCodeGenerator;
    }

    public void setBlobCodeGenerator(Callable<String> blobCodeGenerator) {
        this.blobCodeGenerator = blobCodeGenerator;
    }
}
