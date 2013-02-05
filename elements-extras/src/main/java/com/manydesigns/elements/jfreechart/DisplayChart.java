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

package com.manydesigns.elements.jfreechart;

import org.jfree.chart.servlet.ServletUtilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DisplayChart extends HttpServlet {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";
    public DisplayChart() {
        super();
    }

    public void init() throws ServletException {
    }

    public void service(HttpServletRequest request,
                        HttpServletResponse response)
            throws ServletException, IOException {
        String filename = request.getParameter("f");

        if (filename == null) {
            throw new Error("Parameter 'filename' must be supplied");
        }

        if (filename.contains ("..") || filename.contains ("\\")
                || filename.contains ("/") || !filename.contains("MDChart")) {
            throw new Error("You cannot navigate the File System");
        }

        //  Controllo l'eistenza del file
        File file = new File(System.getProperty("java.io.tmpdir"), filename);
        if (!file.exists()) {
            throw new Error("File '" + file.getAbsolutePath()
                    + "' does not exist");
        }

        ServletUtilities.sendTempFile(file, response);

        //Cancello il file dopo l'invio
        //todo vedere se cancellarlo o no
        //file.delete();
    }
}
