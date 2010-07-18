package com.manydesigns.portofino.logging;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;

/**
 * File created on Jul 18, 2010 at 1:51:30 PM
 * Copyright Paolo Predonzani (paolo.predonzani@gmail.com)
 * All rights reserved
 */
public class LogUtil {
    public static void log(Logger logger, Level level, String format, Object arg) {
        if (logger.isLoggable(level)) {
            logger.log(level, MessageFormat.format(format, arg));
        }
    }

    public static void severe(Logger logger, String format, String arg) {
        log(logger, Level.SEVERE, format, arg);
    }

    public static void warning(Logger logger, String format, Object arg) {
        log(logger, Level.WARNING, format, arg);
    }

    public static void info(Logger logger, String format, Object arg) {
        log(logger, Level.INFO, format, arg);
    }

    public static void config(Logger logger, String format, Object arg) {
        log(logger, Level.CONFIG, format, arg);
    }

    public static void fine(Logger logger, String format, Object arg) {
        log(logger, Level.FINE, format, arg);
    }

    public static void finer(Logger logger, String format, Object arg) {
        log(logger, Level.FINER, format, arg);
    }

    public static void finest(Logger logger, String format, Object arg) {
        log(logger, Level.FINEST, format, arg);
    }

}
