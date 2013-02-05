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

package com.manydesigns.elements.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.ArrayUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class MultipartRequestWrapper extends HttpServletRequestWrapper
    implements MultipartRequest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Map<String, String[]> parameterMap;
    protected final Map<String, FileItem[]> fileItemMap;

    //**************************************************************************
    // Constructor
    //**************************************************************************

    public MultipartRequestWrapper() throws FileUploadException {
        this(null);
    }

    public MultipartRequestWrapper(HttpServletRequest request)
            throws FileUploadException {
        super(request);
        parameterMap = new HashMap<String, String[]>();
        fileItemMap = new HashMap<String, FileItem[]>();
        if (request != null) {
            initilizeFromRequest(request);
        }
    }

    protected void initilizeFromRequest(HttpServletRequest request)
            throws FileUploadException {
        // Copy all request parameters into parameterMap
        //noinspection unchecked
        parameterMap.putAll(request.getParameterMap());

        boolean multipart = ServletFileUpload.isMultipartContent(request);
        if (multipart) {
            parseMultipart(request);
        }
    }

    protected void parseMultipart(HttpServletRequest request)
            throws FileUploadException {
        // Parse the request
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List items = upload.parseRequest(request);
        for (Object item1 : items) {
            FileItem item = (FileItem) item1;
            String parameterName = item.getFieldName();
            if (item.isFormField()) {
                String parameterValue = item.getString();
                addParameter(parameterName, parameterValue);
            } else {
                addFileItem(parameterName, item);
            }
        }
    }

    //**************************************************************************
    // Methods
    //**************************************************************************

    public void addParameter(String name, String value) {
        String[] oldValues = parameterMap.get(name);
        String[] newValues = (String[]) ArrayUtils.add(oldValues, value);
        parameterMap.put(name, newValues);
    }

    public void addFileItem(String name, FileItem item) {
        FileItem[] oldValues = fileItemMap.get(name);
        FileItem[] newValues = (FileItem[]) ArrayUtils.add(oldValues, item);
        fileItemMap.put(name, newValues);
    }


    public FileItem getFileItem(String name) {
        FileItem[] values = fileItemMap.get(name);
        if (values == null) {
            return null;
        } else {
            return values[0];
        }
    }



    //**************************************************************************
    // HttpServletRequest overrides
    //**************************************************************************

    public String getParameter(String name) {
        String[] values = parameterMap.get(name);
        if (values == null || values.length == 0) {
            return null;
        } else {
            return values[0];
        }
    }

    public Enumeration getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    public Map getParameterMap() {
        return parameterMap;
    }
}
