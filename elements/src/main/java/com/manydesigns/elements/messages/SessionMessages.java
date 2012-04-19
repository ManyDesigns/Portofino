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
package com.manydesigns.elements.messages;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*
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

    public static XhtmlBuffer createStringMessage(String msg) {
        XhtmlBuffer xb = new XhtmlBuffer();
        xb.write(msg);
        return xb;
    }

    public static void addInfoMessage(String msg) {
        XhtmlBuffer xb = createStringMessage(msg);
        addInfoMessage(xb);
    }

    public static void addInfoMessage(XhtmlFragment xml) {
        getInfoQueue().add(xml);
    }

    public static void addWarningMessage(String msg) {
        XhtmlBuffer xb = createStringMessage(msg);
        addWarningMessage(xb);
    }

    public static void addWarningMessage(XhtmlFragment xml) {
        getWarningQueue().add(xml);
    }

    public static void addErrorMessage(String msg) {
        XhtmlBuffer xb = createStringMessage(msg);
        addErrorMessage(xb);
    }

    public static void addErrorMessage(XhtmlFragment xml) {
        getErrorQueue().add(xml);
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
}
