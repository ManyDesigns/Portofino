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

package com.manydesigns.portofino.rest;

import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.blobs.BlobManager;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
public class Utilities {

    public static Response downloadBlob(Blob blob, BlobManager blobManager, HttpServletRequest request, Logger logger) {
        if(blob == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if(blob.getInputStream() == null) {
            try {
                blobManager.loadMetadata(blob);
            } catch (IOException e) {
                logger.error("Could not load blob", e);
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        long contentLength = blob.getSize();
        String contentType = blob.getContentType();
        String fileName = blob.getFilename();
        long lastModified = blob.getCreateTimestamp().getMillis();
        if(request.getHeader("If-Modified-Since") != null) {
            long ifModifiedSince = request.getDateHeader("If-Modified-Since");
            if(ifModifiedSince >= lastModified) {
                return Response.status(Response.Status.NOT_MODIFIED).build();
            }
        }
        final InputStream inputStream;
        if(blob.getInputStream() == null) {
            try {
                inputStream = blobManager.openStream(blob);
            } catch (IOException e) {
                logger.error("Could not load blob", e);
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            inputStream = blob.getInputStream();
        }
        StreamingOutput streamingOutput = output -> {
            try(InputStream i = inputStream) {
                IOUtils.copyLarge(i, output);
            }
        };
        Response.ResponseBuilder responseBuilder = Response.ok(streamingOutput).
                type(contentType).
                lastModified(new Date(lastModified)).
                header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        if(contentLength > 0) {
            responseBuilder.header(HttpHeaders.CONTENT_LENGTH, contentLength);
        }
        return responseBuilder.build();
    }

}
