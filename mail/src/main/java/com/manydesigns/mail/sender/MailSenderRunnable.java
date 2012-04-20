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

package com.manydesigns.mail.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MailSenderRunnable implements Runnable {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected final MailSender sender;

    protected boolean alive;
    protected int pollInterval = 1000;

    private static final Logger logger = LoggerFactory.getLogger(MailSenderRunnable.class);

    public MailSenderRunnable(MailSender sender) {
        this.sender = sender;
    }

    public void run() {
        alive = true;
        try {
            mainLoop();
        } catch (InterruptedException e) {
            stop();
        }
    }

    protected void mainLoop() throws InterruptedException {
        Set<String> idsToMarkAsSent = new HashSet<String>();
        int pollIntervalMultiplier = 1;
        while (alive) {
            long now = System.currentTimeMillis();
            int serverErrors = sender.runOnce(idsToMarkAsSent);
            if(serverErrors < 0) {
                continue;
            } else if(serverErrors > 0) {
                if(pollIntervalMultiplier < 10) {
                    pollIntervalMultiplier++;
                    logger.debug("{} server errors, increased poll interval multiplier to {}",
                            serverErrors, pollIntervalMultiplier);
                }
            } else {
                pollIntervalMultiplier = 1;
                logger.debug("No server errors, poll interval multiplier reset");
            }
            long sleep = pollInterval * pollIntervalMultiplier - (System.currentTimeMillis() - now);
            if(sleep > 0) {
                logger.debug("Sleeping for {}ms", sleep);
                Thread.sleep(sleep);
            }
        }
    }

    public void stop() {
        alive = false;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }

    public boolean isAlive() {
        return alive;
    }

}
