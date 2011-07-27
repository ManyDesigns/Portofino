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
package com.manydesigns.portofino.io;

import org.apache.commons.transaction.util.LoggerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Slf4jLoggerFacade implements LoggerFacade{
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected Logger logger;

    public Slf4jLoggerFacade(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    public LoggerFacade createLogger(String name) {
        return new Slf4jLoggerFacade(LoggerFactory.getLogger(name));
    }

    public void logInfo(String message) {
        logger.info(message);
    }

    public void logFine(String message) {
        logger.debug(message);
    }

    public boolean isFineEnabled() {
        return true;
    }

    public void logFiner(String message) {
        logger.debug(message);
    }

    public boolean isFinerEnabled() {
        return true;
    }

    public void logFinest(String message) {
        logger.debug(message);
    }

    public boolean isFinestEnabled() {
        return true;
    }

    public void logWarning(String message) {
        logger.warn(message);
    }

    public void logWarning(String message, Throwable t) {
        logger.warn(message, t);
    }

    public void logSevere(String message) {
        logger.error(message);
    }

    public void logSevere(String message, Throwable t) {
        logger.error(message, t);
    }
}
