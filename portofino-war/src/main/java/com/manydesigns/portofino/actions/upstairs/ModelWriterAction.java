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
package com.manydesigns.portofino.actions.upstairs;

import com.opensymphony.xwork2.ActionSupport;
import com.manydesigns.portofino.context.MDContext;
import com.manydesigns.portofino.model.DataModel;
import com.manydesigns.portofino.model.io.ModelWriter;
import com.manydesigns.portofino.interceptors.MDContextAware;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ModelWriterAction extends ActionSupport implements MDContextAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public MDContext context;
    public DataModel dataModel;
    // result parameters
    public InputStream inputStream;
    String contentType;
    String contentDisposition;

    public String skin = "default";

    public void setContext(MDContext context) {
        this.context = context;
        dataModel = context.getDataModel();
    }

    public String execute() {

        try {
            contentType= "text/xml";
            contentDisposition= MessageFormat.format("inline; filename={0}.xml",
                        "datamodel");
            ModelWriter writer = new ModelWriter(dataModel);
            File tempFile = File.createTempFile("portofino", ".xml");
            writer.write(tempFile);
            inputStream = new FileInputStream(tempFile);
        } catch (IOException e) {
            throw new Error(e);
        }

        return SUCCESS;
    }
}