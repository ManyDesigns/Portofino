/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.blobs;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import static org.apache.commons.io.FileUtils.openOutputStream;

/**
 * MultipartWrapper implementation that uses the streaming API of Commons Fileupload, avoiding the use of files.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class StreamingCommonsMultipartWrapper implements MultipartWrapper {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    private static final Pattern WINDOWS_PATH_PREFIX_PATTERN = Pattern.compile("(?i:^[A-Z]:\\\\)");

    /* Ensure this class will not load unless Commons FileUpload is on the classpath. */
    static {
        FileUploadException.class.getName();
    }

    private Map<String,FileItem> files = new HashMap<String,FileItem>();
    private Map<String,String[]> parameters = new HashMap<String, String[]>();
    private String charset;

    public static class FileItem {
        public final String fileName;
        public final String contentType;
        public final File contents;
        public final int size;

        public FileItem(String fileName, String contentType, File contents, int size) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.contents = contents;
            this.size = size;
        }
    }

    /**
     * Pseudo-constructor that allows the class to perform any initialization necessary.
     *
     * @param request     an HttpServletRequest that has a content-type of multipart.
     * @param tempDir a File representing the temporary directory that can be used to store
     *        file parts as they are uploaded if this is desirable
     * @param maxPostSize the size in bytes beyond which the request should not be read, and a
     *                    FileUploadLimitExceeded exception should be thrown
     * @throws IOException if a problem occurs processing the request of storing temporary
     *                    files
     * @throws FileUploadLimitExceededException if the POST content is longer than the
     *                     maxPostSize supplied.
     */
    @SuppressWarnings("unchecked")
    public void build(HttpServletRequest request, File tempDir, long maxPostSize)
            throws IOException, FileUploadLimitExceededException {
        try {
            this.charset = request.getCharacterEncoding();
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(tempDir);
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(maxPostSize);
            FileItemIterator iterator = upload.getItemIterator(request);

            Map<String,List<String>> params = new HashMap<String, List<String>>();

            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                try(InputStream stream = item.openStream()) {
                    // If it's a form field, add the string value to the list
                    if (item.isFormField()) {
                        List<String> values = params.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>());
                        values.add(charset == null ? IOUtils.toString(stream) : IOUtils.toString(stream, charset));
                    } else {
                        // Else store the file param
                        File tempFile = File.createTempFile("portofino-temp-" + item.getName(), ".temp");
                        int size = copyInputStreamToFile(stream, tempFile);
                        FileItem fileItem = new FileItem(item.getName(), item.getContentType(), tempFile, size);
                        files.put(item.getFieldName(), fileItem);
                    }
                }
            }

            // Now convert them down into the usual map of String->String[]
            for (Map.Entry<String,List<String>> entry : params.entrySet()) {
                List<String> values = entry.getValue();
                this.parameters.put(entry.getKey(), values.toArray(new String[0]));
            }
        }
        catch (FileUploadBase.SizeLimitExceededException slee) {
            throw new FileUploadLimitExceededException(maxPostSize, slee.getActualSize());
        }
        catch (FileUploadException fue) {
            throw new IOException("Could not parse and cache file upload data.", fue);
        }

    }

    public static int copyInputStreamToFile(InputStream source, File destination) throws IOException {
        try(InputStream input = source; FileOutputStream output = openOutputStream(destination)) {
            return IOUtils.copy(input, output);
        }
    }

    /**
     * Fetches the names of all non-file parameters in the request. Directly analogous to the
     * method of the same name in HttpServletRequest when the request is non-multipart.
     *
     * @return an Enumeration of all non-file parameter names in the request
     */
    public Enumeration<String> getParameterNames() {
        return new IteratorEnumeration(this.parameters.keySet().iterator());
    }

    /**
     * Fetches all values of a specific parameter in the request. To simulate the HTTP request
     * style, the array should be null for non-present parameters, and values in the array should
     * never be null - the empty String should be used when there is value.
     *
     * @param name the name of the request parameter
     * @return an array of non-null parameters or null
     */
    public String[] getParameterValues(String name) {
        return this.parameters.get(name);
    }

    /**
     * Fetches the names of all file parameters in the request. Note that these are not the file
     * names, but the names given to the form fields in which the files are specified.
     *
     * @return the names of all file parameters in the request.
     */
    public Enumeration<String> getFileParameterNames() {
        return new IteratorEnumeration(this.files.keySet().iterator());
    }

    /**
     * Responsible for constructing a FileBean object for the named file parameter. If there is no
     * file parameter with the specified name this method should return null.
     *
     * @param name the name of the file parameter
     * @return a FileBean object wrapping the uploaded file
     */
    public FileBean getFileParameterValue(String name) {
        final FileItem item = this.files.get(name);

        if (item == null
                || ((item.fileName == null || item.fileName.length() == 0) && item.size == 0)) {
            return null;
        } else {
            // Attempt to ensure the file name is just the basename with no path included
            String filename = item.fileName;
            if(filename != null) {
                int index;
                if (WINDOWS_PATH_PREFIX_PATTERN.matcher(filename).find()) {
                    index = filename.lastIndexOf('\\');
                } else {
                    index = filename.lastIndexOf('/');
                }
                if (index >= 0 && index + 1 < filename.length() - 1) {
                    filename = filename.substring(index + 1);
                }
            }

            // Use an anonymous inner subclass of FileBean that overrides all the
            // methods that rely on having a File present, to use the FileItem
            // created by commons upload instead.
            return new FileBean(null, item.contentType, filename, this.charset) {
                @Override
                public long getSize() {
                    return item.size;
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(item.contents);
                }

                @Override
                public void delete() throws IOException {
                    item.contents.delete();
                }
            };
        }
    }

    @Override
    public HttpServletRequestWrapper wrapRequest(HttpServletRequest request) {
        return new MultipartRequest(request, this);
    }

}
