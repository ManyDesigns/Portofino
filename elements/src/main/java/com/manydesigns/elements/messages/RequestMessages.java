/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.messages;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** Stores messages as HTTP request attributes, to be sent as headers in the response.
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class RequestMessages {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";
    public static final String INFO_MESSAGES_KEY = "info_messages_key";
    public static final String WARNING_MESSAGES_KEY = "warning_messages_key";
    public static final String ERROR_MESSAGES_KEY = "error_messages_key";

    public final static Logger logger =
            LoggerFactory.getLogger(RequestMessages.class);

    public static void addInfoMessage(String msg) {
        getInfoQueue().add(msg);
    }

    public static void addInfoMessage(XhtmlFragment xml) {
        XhtmlBuffer buf = new XhtmlBuffer();
        xml.toXhtml(buf);
        getInfoQueue().add(buf.toString());
    }

    public static void addWarningMessage(String msg) {
        getWarningQueue().add(msg);
    }

    public static void addWarningMessage(XhtmlFragment xml) {
        XhtmlBuffer buf = new XhtmlBuffer();
        xml.toXhtml(buf);
        getWarningQueue().add(buf.toString());
    }

    public static void addErrorMessage(String msg) {
        getErrorQueue().add(msg);
    }

    public static void addErrorMessage(XhtmlFragment xml) {
        XhtmlBuffer buf = new XhtmlBuffer();
        xml.toXhtml(buf);
        getErrorQueue().add(buf.toString());
    }

    public static List<String> consumeInfoMessages() {
        List<String> result = new ArrayList<>();
        getInfoQueue().drainTo(result);
        return result;
    }

    public static List<String> consumeWarningMessages() {
        List<String> result = new ArrayList<>();
        getWarningQueue().drainTo(result);
        return result;
    }

    public static List<String> consumeErrorMessages() {
        List<String> result = new ArrayList<>();
        getErrorQueue().drainTo(result);
        return result;
    }

    public static BlockingQueue<String> getInfoQueue() {
        return getQueue(INFO_MESSAGES_KEY);
    }

    public static BlockingQueue<String> getWarningQueue() {
        return getQueue(WARNING_MESSAGES_KEY);
    }

    public static BlockingQueue<String> getErrorQueue() {
        return getQueue(ERROR_MESSAGES_KEY);
    }

    protected static BlockingQueue<String> getQueue(String queueName) {
        HttpServletRequest req = ElementsThreadLocals.getHttpServletRequest();
        if (req == null) {
            logger.debug("No request available. Returning dummy queue.");
            return new LinkedBlockingQueue<>();
        }
        BlockingQueue<String> queue;
        synchronized (req) {
            queue = (BlockingQueue) req.getAttribute(queueName);
            if (queue == null) {
                // install a new queue
                queue = new LinkedBlockingQueue<>();
                req.setAttribute(queueName, queue);
            }
        }
        return queue;
    }

}
