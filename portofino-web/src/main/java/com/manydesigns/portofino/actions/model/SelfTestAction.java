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

package com.manydesigns.portofino.actions.model;

import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.model.Model;
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
