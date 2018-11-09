/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.manydesigns.elements.blobs;

import javax.servlet.ServletException;

/**
 * Exception that is thrown when the post size of a multipart/form post used for file
 * upload exceeds the configured maximum size.
 *
 * @author Tim Fennell
 */
public class FileUploadLimitExceededException extends ServletException {
    private static final long serialVersionUID = 1L;

    private long maximum;
    private long posted;

    /**
     * Constructs a new exception that contains the limit that was violated, and the size
     * of the post that violated it, both in bytes.
     *
     * @param max the current post size limit
     * @param posted the size of the post
     */
    public FileUploadLimitExceededException(long max, long posted) {
        super("File post limit exceeded. Limit: " + max + " bytes. Posted: " + posted + " bytes.");
        this.maximum = max;
        this.posted = posted;
    }

    /** Gets the limit in bytes for HTTP POSTs. */
    public long getMaximum() { return maximum; }

    /** The size in bytes of the HTTP POST. */
    public long getPosted() { return posted; }
}