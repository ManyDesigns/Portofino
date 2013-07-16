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

package com.manydesigns.mail.stripes;

import com.manydesigns.mail.sender.MailSender;
import com.manydesigns.portofino.modules.MailModule;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.Resolution;
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
public abstract class SendMailAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

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

        MailSender mailSender = (MailSender) context.getServletContext().getAttribute(MailModule.MAIL_SENDER);
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
