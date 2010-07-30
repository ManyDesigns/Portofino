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

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class CustomFormatter extends Formatter {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";


    // milliseconds can be nice for rough performance numbers
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS Z";
    private static final String DEFAULT_FORMAT = "%L: %n %m";
    private final MessageFormat messageFormat;
    private final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    public CustomFormatter() {
        super();
        // load the format from logging.properties
        String propName = getClass().getName() + ".format";
        String format = LogManager.getLogManager().getProperty(propName);
        if (format == null || format.trim().length() == 0)
            format = DEFAULT_FORMAT;
        if (format.contains("{") || format.contains("}"))
            throw new IllegalArgumentException("curly braces not allowed");
        // convert it into the MessageFormat form
        format = format
        .replace("%L", "{0}")
        .replace("%m", "{1}")
        .replace("%M", "{2}")
        .replace("%t", "{3}")
        .replace("%c", "{4}")
        .replace("%T", "{5}")
        .replace("%n", "{6}")
        .replace("%C", "{7}") + "\n";
        messageFormat = new MessageFormat(format);
    }

    
    @Override
    public String format(LogRecord record) {
        String[] arguments = new String[9];
        // %L
        arguments[0] = record.getLevel().toString();
        // %m
        arguments[1] = record.getMessage();
        // sometimes the message is empty, but there is a throwable
        if (arguments[1] == null || arguments[1].length() == 0) {
            Throwable thrown = record.getThrown();
            if (thrown != null) {
                arguments[1] = thrown.getMessage();
            }
        }
        // %M
        if (record.getSourceMethodName() != null) {
            arguments[2] = record.getSourceMethodName();
        } else {
            arguments[2] = "?";
        }
        // %t
        Date date = new Date(record.getMillis());
        synchronized (dateFormat) {
            arguments[3] = dateFormat.format(date);
        }
        // %c
        if (record.getSourceClassName() != null) {
            arguments[4] = record.getSourceClassName();
        } else {
            arguments[4] = "?";
        }
        // %T
        arguments[5] = Integer.valueOf(record.getThreadID()).toString();
        // %n
        arguments[6] = record.getLoggerName();
        // %C
        int start = arguments[4].lastIndexOf(".") + 1;
        if (start > 0 && start < arguments[4].length()) {
            arguments[7] = arguments[4].substring(start);
        } else {
            arguments[7] = arguments[4];
        }
        synchronized (messageFormat) {
            return messageFormat.format(arguments);
        }
    }
}