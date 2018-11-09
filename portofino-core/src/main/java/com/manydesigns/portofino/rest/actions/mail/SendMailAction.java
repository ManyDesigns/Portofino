/*
* Copyright (C) 2005-2017 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.rest.actions.mail;

import com.manydesigns.mail.sender.MailSender;
import com.manydesigns.portofino.modules.MailModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Path("/actions/mail-sender-run")
public class SendMailAction {
    public static final String copyright =
            "Copyright (C) 2005-2017 ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(SendMailAction.class);

    @Context
    protected ServletContext servletContext;

    @Context
    protected HttpServletRequest request;

    @Autowired(required = false)
    protected MailSender mailSender;

    @GET
    public Response execute() {
        String clientIP = request.getRemoteAddr();
        try {
            InetAddress clientAddr = InetAddress.getByName(clientIP);
            if(!isLocalIPAddress(clientAddr)) {
                logger.warn("Received request from non-local addr, forbidding access: {}", clientAddr);
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (UnknownHostException e) {
            logger.error("Could not determine request address", e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if(mailSender == null) {
            return Response.serverError().entity("Mail Sender not active").build();
        }
        logger.debug("Sending pending email messages");
        HashSet<String> idsToMarkAsSent = new HashSet<String>();
        int serverErrors = mailSender.runOnce(idsToMarkAsSent);
        if(serverErrors < 0) {
            logger.warn("Mail sender did not run.");
        } else if(serverErrors > 0) {
            logger.warn("Mail sender encountered {} server errors.", serverErrors);
        }
        if(!idsToMarkAsSent.isEmpty()) {
            logger.warn("The following email(s) were sent but could not be marked as sent; they will be sent twice! {}", idsToMarkAsSent);
        }

        return null;
    }

    public static boolean isLocalIPAddress(InetAddress addr) {
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
            return true;
        }

        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            return false;
        }
    }
}
