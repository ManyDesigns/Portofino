/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.model;

import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.dispatcher.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.model.datamodel.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class SelfTestAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    @Inject(RequestAttributes.MODEL)
    public Model model;

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    // result parameters
    public InputStream inputStream;
    public String contentType;
    public String contentDisposition;

    public boolean showBothNull = false;
    public boolean showSourceNull = true;
    public boolean showTargetNull = true;
    public boolean showEqual = false;
    public boolean showDifferent = true;

    public boolean expandTree = true;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(SelfTestAction.class);

    //--------------------------------------------------------------------------
    // Action methods
    //--------------------------------------------------------------------------

    public String execute() throws SQLException {
        return "SUCCESS";
    }

    public String export() throws Exception {
        contentType= "text/xml";
        contentDisposition= MessageFormat.format("inline; filename={0}.xml",
                    "datamodel");
        File tempFile = File.createTempFile("portofino", ".xml");
        JAXBContext jc = JAXBContext.newInstance(Model.JAXB_MODEL_PACKAGES);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(model, tempFile);
        inputStream = new FileInputStream(tempFile);
        return "export";
    }


}
