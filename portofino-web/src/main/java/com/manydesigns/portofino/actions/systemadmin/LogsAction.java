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

package com.manydesigns.portofino.actions.systemadmin;

import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.servlets.ServerInfo;
import com.manydesigns.portofino.di.Inject;

import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class LogsAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public LogManager logManager;
    public List<Logger> loggers;
    public Set<Handler> handlers;

    public String execute() {
        logManager = LogManager.getLogManager();
        loggers = new ArrayList<Logger>();
        handlers = new HashSet<Handler>();

        Enumeration<String> loggerNames = logManager.getLoggerNames();
        while (loggerNames.hasMoreElements()) {
            String loggerName = loggerNames.nextElement();
            Logger logger = logManager.getLogger(loggerName);
            loggers.add(logger);

            Handler[] handlers = logger.getHandlers();
            this.handlers.addAll(Arrays.asList(handlers));
        }
        Collections.sort(loggers, new LoggerComparator());
        return "SUCCESS";
    }

    @Inject(ApplicationAttributes.SERVER_INFO)
    public ServerInfo serverInfo;

    public class LoggerComparator implements Comparator<Logger> {
        public int compare(Logger l1, Logger l2) {
            return l1.getName().compareTo(l2.getName());
        }
    }
}
