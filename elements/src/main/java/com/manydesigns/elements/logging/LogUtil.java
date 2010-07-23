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

package com.manydesigns.elements.logging;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class LogUtil {
    public static final Level METHOD_ENTERING_EXITING_LEVEL = Level.FINER;

    //**************************************************************************
    // Retrieving loggers
    //**************************************************************************

    public static Logger getLogger(Class clazz) {
        return Logger.getLogger(clazz.getName());
    }

    //**************************************************************************
    // Logging with MessageFormat
    //**************************************************************************
    public static void logMF(Logger logger, Level level,
                             String format, Object... args) {
        if (logger.isLoggable(level)) {
            logger.log(level, MessageFormat.format(format, args));
        }
    }

    public static void severeMF(Logger logger, String format, Object... args) {
        logMF(logger, Level.SEVERE, format, args);
    }

    public static void warningMF(Logger logger, String format, Object... args) {
        logMF(logger, Level.WARNING, format, args);
    }

    public static void infoMF(Logger logger, String format, Object... args) {
        logMF(logger, Level.INFO, format, args);
    }

    public static void configMF(Logger logger, String format, Object... args) {
        logMF(logger, Level.CONFIG, format, args);
    }

    public static void fineMF(Logger logger, String format, Object... args) {
        logMF(logger, Level.FINE, format, args);
    }

    public static void finerMF(Logger logger, String format, Object... args) {
        logMF(logger, Level.FINER, format, args);
    }

    public static void finestMF(Logger logger, String format, Object... args) {
        logMF(logger, Level.FINEST, format, args);
    }


    //**************************************************************************
    // Logging exceptions
    //**************************************************************************

    public static void severe(Logger logger, Throwable e, String message) {
        logger.log(Level.SEVERE, message, e);
    }

    public static void warning(Logger logger, Throwable e, String message) {
        logger.log(Level.WARNING, message, e);
    }

    public static void info(Logger logger, Throwable e, String message) {
        logger.log(Level.INFO, message, e);
    }

    public static void config(Logger logger, Throwable e, String message) {
        logger.log(Level.CONFIG, message, e);
    }

    public static void fine(Logger logger, Throwable e, String message) {
        logger.log(Level.FINE, message, e);
    }

    public static void finer(Logger logger, Throwable e, String message) {
        logger.log(Level.FINER, message, e);
    }

    public static void finest(Logger logger, Throwable e, String message) {
        logger.log(Level.FINEST, message, e);
    }


    //**************************************************************************
    // Logging exceptions with MessageFormat
    //**************************************************************************

    public static void logMF(Logger logger, Level level,
                             Throwable e, String format, Object... args) {
        if (logger.isLoggable(level)) {
            logger.log(level, MessageFormat.format(format, args), e);
        }
    }

    public static void severeMF(Logger logger, Throwable e,
                                String format, Object... args) {
        logMF(logger, Level.SEVERE, e, format, args);
    }

    public static void warningMF(Logger logger, Throwable e,
                                 String format, Object... args) {
        logMF(logger, Level.WARNING, e, format, args);
    }

    public static void infoMF(Logger logger, Throwable e,
                              String format, Object... args) {
        logMF(logger, Level.INFO, e, format, args);
    }

    public static void configMF(Logger logger, Throwable e,
                                String format, Object... args) {
        logMF(logger, Level.CONFIG, e, format, args);
    }

    public static void fineMF(Logger logger, Throwable e,
                              String format, Object... args) {
        logMF(logger, Level.FINE, e, format, args);
    }

    public static void finerMF(Logger logger, Throwable e,
                               String format, Object... args) {
        logMF(logger, Level.FINER, e, format, args);
    }

    public static void finestMF(Logger logger, Throwable e,
                                String format, Object... args) {
        logMF(logger, Level.FINEST, e, format, args);
    }


    //**************************************************************************
    // Log entering/exiting methods
    //**************************************************************************
    public static void entering(Logger logger, String methodName, Object... args) {
        if (logger.isLoggable(METHOD_ENTERING_EXITING_LEVEL)) {
            StringBuilder sb = new StringBuilder("Entering ");
            sb.append(methodName);
            sb.append("(");
            boolean first = true;
            for (Object arg : args) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" , ");
                }
                if (arg == null) {
                    sb.append("null");
                } else {
                    if (arg instanceof String) {
                        sb.append("\"");
                        sb.append(arg.toString());
                        sb.append("\"");
                    } else {
                        sb.append(arg.toString());
                    }
                }
            }
            sb.append(")");
            logger.log(METHOD_ENTERING_EXITING_LEVEL, sb.toString());
        }
    }

    public static void exiting(Logger logger, String methodName) {
        logMF(logger, METHOD_ENTERING_EXITING_LEVEL, "Exiting {0}", methodName);
    }

}
