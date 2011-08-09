/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.systemadmin;

import com.manydesigns.portofino.actions.AbstractActionBean;
import com.manydesigns.portofino.annotations.InjectServerInfo;
import com.manydesigns.portofino.context.ServerInfo;

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
            "Copyright (c) 2005-2011, ManyDesigns srl";

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

    @InjectServerInfo
    public ServerInfo serverInfo;

    public class LoggerComparator implements Comparator<Logger> {
        public int compare(Logger l1, Logger l2) {
            return l1.getName().compareTo(l2.getName());
        }
    }
}
