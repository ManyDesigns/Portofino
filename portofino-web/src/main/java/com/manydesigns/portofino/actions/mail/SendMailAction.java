/*
* Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.mail;

import com.manydesigns.mail.sender.MailSender;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@UrlBinding("/actions/mail-sender-run")
public class SendMailAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(SendMailAction.class);

    @DefaultHandler
    public Resolution execute() {
        String clientIP = context.getRequest().getRemoteAddr();
        try {
            InetAddress clientAddr = InetAddress.getByName(clientIP);
            if(!isLocalIPAddress(clientAddr)) {
                logger.warn("Received request from non-local addr, forbidding access: {}", clientAddr);
                return new ErrorResolution(403);
            }
        } catch (UnknownHostException e) {
            logger.error("Could not determine request address", e);
            return new ErrorResolution(403);
        }

        MailSender mailSender = (MailSender) context.getServletContext().getAttribute(ApplicationAttributes.MAIL_SENDER);
        if(mailSender == null) {
            return new ErrorResolution(500, "Mail Sender not active");
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
