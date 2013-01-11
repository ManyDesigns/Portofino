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
package com.manydesigns.elements.messages;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SessionMessages {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";
    public static final String INFO_MESSAGES_KEY = "info_messages_key";
    public static final String WARNING_MESSAGES_KEY = "warning_messages_key";
    public static final String ERROR_MESSAGES_KEY = "error_messages_key";

    public final static Logger logger =
            LoggerFactory.getLogger(SessionMessages.class);

    public static void addInfoMessage(String msg) {
        getInfoQueue().add(new Message(msg));
    }

    public static void addInfoMessage(XhtmlFragment xml) {
        getInfoQueue().add(new Message(xml));
    }

    public static void addWarningMessage(String msg) {
        getWarningQueue().add(new Message(msg));
    }

    public static void addWarningMessage(XhtmlFragment xml) {
        getWarningQueue().add(new Message(xml));
    }

    public static void addErrorMessage(String msg) {
        getErrorQueue().add(new Message(msg));
    }

    public static void addErrorMessage(XhtmlFragment xml) {
        getErrorQueue().add(new Message(xml));
    }

    public static List<XhtmlFragment> consumeInfoMessages() {
        List<XhtmlFragment> result = new ArrayList<XhtmlFragment>();
        getInfoQueue().drainTo(result);
        return result;
    }

    public static List<XhtmlFragment> consumeWarningMessages() {
        List<XhtmlFragment> result = new ArrayList<XhtmlFragment>();
        getWarningQueue().drainTo(result);
        return result;
    }

    public static List<XhtmlFragment> consumeErrorMessages() {
        List<XhtmlFragment> result = new ArrayList<XhtmlFragment>();
        getErrorQueue().drainTo(result);
        return result;
    }

    public static BlockingQueue<XhtmlFragment> getInfoQueue() {
        return getQueue(INFO_MESSAGES_KEY);
    }

    public static BlockingQueue<XhtmlFragment> getWarningQueue() {
        return getQueue(WARNING_MESSAGES_KEY);
    }

    public static BlockingQueue<XhtmlFragment> getErrorQueue() {
        return getQueue(ERROR_MESSAGES_KEY);
    }

    protected static BlockingQueue<XhtmlFragment> getQueue(String queueName) {
        HttpServletRequest req = ElementsThreadLocals.getHttpServletRequest();
        if (req == null) {
            logger.debug("No request available. Returning dummy queue.");
            return new LinkedBlockingQueue<XhtmlFragment>();
        }
        HttpSession session = req.getSession();
        BlockingQueue<XhtmlFragment> infoQueue;
        synchronized (session) {
            infoQueue = (BlockingQueue)session.getAttribute(queueName);
            if (infoQueue == null) {
                // install a new queue
                infoQueue = new LinkedBlockingQueue<XhtmlFragment>();
                session.setAttribute(queueName, infoQueue);
            }
        }
        return infoQueue;
    }

    /**
     * Guscio amichevole verso sessioni persistenti (es. in Tomcat).
     * Contiene un XhtmlFragment transient.
     */
    public static class Message implements XhtmlFragment, Serializable {

        protected XhtmlFragment delegate;
        protected String string;

        public Message(String string) {
            this.string = string;
        }

        public Message(XhtmlFragment delegate) {
            this.delegate = delegate;
        }

        public void toXhtml(@NotNull XhtmlBuffer xb) {
            if(string != null) {
                xb.writeNoHtmlEscape(string);
            } else if(delegate != null) {
                delegate.toXhtml(xb);
            } else {
                logger.warn("Empty message");
            }
        }

        /*
         * On serialization, if delegate is not null and not serializable, realize it to a string.
         */
        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            if(delegate != null && !(delegate instanceof Serializable)) {
                XhtmlBuffer buf = new XhtmlBuffer();
                delegate.toXhtml(buf);
                string = buf.toString();
                delegate = null;
            }
            out.defaultWriteObject();
        }

    }
}
