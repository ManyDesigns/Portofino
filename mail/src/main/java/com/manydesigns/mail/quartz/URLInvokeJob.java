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

package com.manydesigns.mail.quartz;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@DisallowConcurrentExecution
public class URLInvokeJob implements Job {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(URLInvokeJob.class);
    public static final String URL_KEY = "url";

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String urlToInvoke = null;
        try {
            urlToInvoke = jobExecutionContext.getMergedJobDataMap().get(URL_KEY).toString();
            logger.debug("URL to invoke: " + urlToInvoke);
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(urlToInvoke).openConnection();
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            if(responseCode != 200) {
                logger.warn("Invocation of URL " + urlToInvoke + " returned response code " + responseCode);
            }
        } catch (Exception e) {
            logger.error("Failed to invoke URL: " + urlToInvoke, e);
            throw new JobExecutionException(e);
        }
    }
}
