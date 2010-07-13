/*
 * Copyright (C) 2005-2009 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.jfreechart;

import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.chart.servlet.ChartDeleter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.File;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class DisplayChart extends HttpServlet {
    public static final String copyright =
            "Copyright (c) 2005-2009, ManyDesigns srl";
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
