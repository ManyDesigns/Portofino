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

package com.manydesigns.elements.json;

import com.manydesigns.elements.ognl.OgnlUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class JsonBuffer {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Static fields
    //**************************************************************************

    public static boolean checkWellFormed = false;

    public Logger logger = LoggerFactory.getLogger(JsonBuffer.class);

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Writer writer;
    protected boolean first;

    //**************************************************************************
    // Constructors
    //**************************************************************************


    /**
     * Creates a new instance of JsonBuffer
     */
    public JsonBuffer() {
        this(new StringWriter());
    }

    /**
     * Creates a new instance of JsonBuffer
     * @param writer
     */
    public JsonBuffer(Writer writer) {
        this.writer = writer;
        first = true;
    }

    //**************************************************************************
    // Public methods
    //**************************************************************************

    public void openArray() {
        try {
            writeCommaIfNeeded();
            writer.write("[");
        } catch (IOException e) {
            logger.error("Json writer exception", e);
        }
        first = true;
    }

    public void closeArray() {
        try {
            writer.write("]");
        } catch (IOException e) {
            logger.error("Json writer exception", e);
        }
        first = false;
    }

    public void openObject() {
        try {
            writeCommaIfNeeded();
            writer.write("{");
        } catch (IOException e) {
            logger.error("Json writer exception", e);
        }
        first = true;
    }

    public void closeObject() {
        try {
            writer.write("}");
        } catch (IOException e) {
            logger.error("Json writer exception", e);
        }
        first = false;
    }

    public void writeKeyValue(String key, String value) {
        String rawValue = MessageFormat.format("\"{0}\"",
                StringEscapeUtils.escapeJava(value));
        writeKeyRawValue(key, rawValue);
    }

    public void writeKeyValue(String key, Integer intValue) {
        String rawValue = OgnlUtils.convertValueToString(intValue);
        writeKeyRawValue(key, rawValue);
    }

    public void writeKeyValue(String key, Boolean booleanValue) {
        String rawValue;
        if (booleanValue == null) {
            rawValue = null;
        } else if (booleanValue) {
            rawValue = "true";
        } else {
            rawValue = "false";
        }
        writeKeyRawValue(key, rawValue);
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    protected void writeKeyRawValue(String key, String rawValue) {
        if (rawValue == null) {
            rawValue = "null";
        }
        try {
            writeCommaIfNeeded();
            String text = MessageFormat.format("\"{0}\":{1}",
                    StringEscapeUtils.escapeJava(key),
                    rawValue);
            writer.write(text);
        } catch (IOException e) {
            logger.error("Json writer exception", e);
        }
    }

    protected void writeCommaIfNeeded() throws IOException {
        if (first) {
            first = false;
        } else {
            writer.write(",");
        }
    }
    
    public String toString() {
        return writer.toString();
    }

}
