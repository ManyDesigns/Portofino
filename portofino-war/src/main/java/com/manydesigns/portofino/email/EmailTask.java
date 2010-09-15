/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino.email;

import com.manydesigns.portofino.context.Context;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/


public class EmailTask extends TimerTask {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final int N_THREADS=5;
    private static ExecutorService outbox = Executors.newFixedThreadPool
            (N_THREADS);
    protected static final ConcurrentLinkedQueue<EmailSender> successQueue
            = new ConcurrentLinkedQueue<EmailSender>();
    protected static final ConcurrentLinkedQueue<EmailSender> rejectedQueue
            = new ConcurrentLinkedQueue<EmailSender>();
    private final POP3Client client;
    private final Context context;

    public EmailTask(Context context) {
        this.context=context;
        client = null;
    }

    public static void stop() {
        outbox.shutdownNow();
    }


    public void run() {

        synchronized (this) {
            createQueue();
            checkBounce();
            manageSuccessAndRejected();
        }
    }

    public synchronized void createQueue() {
        List<Object> emails = context.getAllObjects("portofino.emailqueue");
        for (Object obj : emails) {
            Map email = (Map) obj;
            EmailSender emailSender = new EmailSender(email);
        outbox.submit(emailSender);
        }
    }


    private synchronized void manageSuccessAndRejected() {



        while (!successQueue.isEmpty()) {


            }

        while (!rejectedQueue.isEmpty()) {
            EmailSender email = rejectedQueue.poll();

            outbox.submit(email);
        }
    }


    public static synchronized void resetOutbox()
            throws Exception {
    }

    

    private synchronized void checkBounce() {
        if (client != null) {
            Set<String> emails = client.read();

            for (String email : emails) {
                //incrementBounce(email);
            }

        }
    }


}
