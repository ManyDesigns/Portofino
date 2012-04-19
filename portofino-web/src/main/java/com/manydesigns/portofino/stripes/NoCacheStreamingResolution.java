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

package com.manydesigns.portofino.stripes;

import net.sourceforge.stripes.action.StreamingResolution;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class NoCacheStreamingResolution extends StreamingResolution {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public NoCacheStreamingResolution(String contentType) {
        super(contentType);
        setLastModified(System.currentTimeMillis());
    }

    public NoCacheStreamingResolution(String contentType, InputStream inputStream) {
        super(contentType, inputStream);
        setLastModified(System.currentTimeMillis());
    }

    public NoCacheStreamingResolution(String contentType, Reader reader) {
        super(contentType, reader);
        setLastModified(System.currentTimeMillis());
    }

    public NoCacheStreamingResolution(String contentType, String output) {
        super(contentType, output);
        setLastModified(System.currentTimeMillis());
    }

    @Override
    protected void applyHeaders(HttpServletResponse response) {
        super.applyHeaders(response);

        // Avoid caching of dynamic pages
        response.setHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "must-revalidate");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
    }
}
