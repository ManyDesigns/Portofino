/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.blobs.BlobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Session listener that deletes temporary blobs when a user's session ends. The application must record the blobs it
 * wants to be deleted with the method recordBlob, and can at any time declare that a blob must not be deleted with
 * the method forgetBlob.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class BlobCleanupListener implements HttpSessionListener {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final String SESSION_ATTRIBUTE = BlobCleanupListener.class.getName();

    public static final Logger logger = LoggerFactory.getLogger(BlobCleanupListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        httpSessionEvent.getSession().setAttribute(SESSION_ATTRIBUTE, new ConcurrentSkipListSet());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();

        Set<String> blobs = (Set<String>) session.getAttribute(SESSION_ATTRIBUTE);
        BlobManager blobManager = ElementsThreadLocals.getBlobManager();
        if(blobManager == null) { //Outside of a request
            //The blobs directory is already globally set by PortofinoListener
            blobManager = BlobManager.createDefaultBlobManager();
        }
        for(String blobCode : blobs) {
            logger.info("Deleting unused blob: " + blobCode);
            if(!blobManager.deleteBlob(blobCode)) {
                logger.warn("Could not delete blob " + blobCode);
            }
        }
    }

    public static void recordBlob(Blob blob) {
        if(blob == null) {
            return;
        }
        HttpSession session = ElementsThreadLocals.getHttpServletRequest().getSession();
        Set<String> blobs = (Set<String>) session.getAttribute(SESSION_ATTRIBUTE);
        blobs.add(blob.getCode());
    }

    public static void forgetBlob(Blob blob) {
        if(blob == null) {
            return;
        }
        HttpSession session = ElementsThreadLocals.getHttpServletRequest().getSession();
        Set<String> blobs = (Set<String>) session.getAttribute(SESSION_ATTRIBUTE);
        blobs.remove(blob.getCode());
    }
}
