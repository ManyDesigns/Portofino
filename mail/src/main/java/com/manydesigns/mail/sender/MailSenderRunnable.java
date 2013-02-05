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
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final MailSender sender;

    protected boolean alive;
    protected int pollInterval = 1000;

    public static final Logger logger = LoggerFactory.getLogger(MailSenderRunnable.class);

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
