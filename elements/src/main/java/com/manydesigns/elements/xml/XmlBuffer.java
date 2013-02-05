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


package com.manydesigns.elements.xml;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * @author Paolo Predonzani - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo      - angelo.lupo@manydesigns.com
 */
public class XmlBuffer {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";
    
    private static final int TEXT = 3;
    private static final int START = 0;
    private static final int OPEN = 2;
    private static final int CLOSE = 1;

    //**************************************************************************
    // Static fields
    //**************************************************************************

    public static boolean checkWellFormed = false;
    public static String[] DEFAULT_EMPTY_TAG_ALLOWED_LIST = {};

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Writer writer;
    protected int state;
    protected final Stack<String> tagStack;
    protected String[] allowedEmptyTags;


    //**************************************************************************
    // Constructors
    //**************************************************************************


    /**
     * Creates a new instance of XmlBuffer
     */
    public XmlBuffer() {
        this(new StringWriter());
    }

    /**
     * Creates a new instance of XmlBuffer
     * @param writer The writer
     */
    public XmlBuffer(Writer writer) {
        this.writer = writer;
        state = START;
        if (checkWellFormed) {
            tagStack = new Stack<String>();
        } else {
            tagStack = null;
        }
        allowedEmptyTags = DEFAULT_EMPTY_TAG_ALLOWED_LIST;
    }

    //~--- methods ------------------------------------------------------------

    public void addAttribute(String name, String value) {
        try {
            switch (state) {
                case OPEN:
                    if (value != null) {
                        writer.write(" ");
                        writer.write(name);
                        writer.write("=\"");
                        writer.write(escape(value));
                        writer.write("\"");
                    }
                    break;

                default:
                    throw new IllegalStateException("XmlBuffer state " + state);
            }
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    private void adjust() {
        try {
            switch (state) {
                case OPEN:
                    writer.write(">");
                    state = TEXT;
                case START:
                case CLOSE:
                case TEXT:
                    break;

                default:
                    throw new IllegalStateException("XmlBuffer state " + state);
            }
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public void closeElement(String name) {
        if (tagStack != null) {
            String topOfStack;
            try {
                topOfStack = tagStack.pop();
            } catch(EmptyStackException e) {
                throw new IllegalStateException(
                        "Stack underflow: " + writer.toString(), e);
            }
            if (!topOfStack.equals(name)) {
                throw new IllegalStateException(MessageFormat.format(
                        "Expected: {0} - Actual: {1}\n{2}",
                        topOfStack, name, writer.toString()));
            }
        }
        try {
            switch (state) {
                case OPEN:
                    if (ArrayUtils.contains(allowedEmptyTags, name)) {
                        writer.write(" />");
                    } else {
                        writer.write(">");
                        writer.write("</");
                        writer.write(name);
                        writer.write(">");
                    }
                    break;
                case CLOSE:
                case TEXT:
                    writer.write("</");
                    writer.write(name);
                    writer.write(">");

                    break;

                default:
                    throw new IllegalStateException("XmlBuffer state " + state);
            }

            state = CLOSE;
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public void openElement(String name) {
        try {
            switch (state) {
                case OPEN:
                    writer.write(">");
                case START:
                case CLOSE:
                case TEXT:
                    writer.write("<");
                    writer.write(name);

                    break;

                default:
                    throw new IllegalStateException("XmlBuffer state " + state);
            }

            state = OPEN;
        } catch (IOException e) {
            throw new IOError(e);
        }
        if (tagStack != null) {
            tagStack.push(name);
        }
    }

    public void write(String text) {
        writeNoHtmlEscape(escape(text));
    }

    public void write(XmlBuffer buffer) {
        try {
            switch (state) {
                case OPEN:
                    adjust();
                case START:
                case CLOSE:
                case TEXT:
                    if (buffer != null) {
                        writer.write(buffer.writer.toString());
                    }

                    break;

                default:
                    throw new IllegalStateException("XmlBuffer state " + state);
            }
            if (buffer != null && buffer.state != START) {
                state = buffer.state;
                if (tagStack != null) {
                    tagStack.addAll(buffer.tagStack);
                }
            }
            //else lascia lo stato esistente
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public void writeXmlHeader(String encoding) {
        try {
            switch (state) {
                case START:
                    writer.write("<?xml version=\"1.0\" encoding=\"");
                    writer.write(encoding);
                    writer.write("\"?>");
                    break;

                default:
                    throw new IllegalStateException("XmlBuffer state " + state);
            }

            state = CLOSE;
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public void writeDoctype(String first, String second, String third,
                             String fourth) {
        try {
            switch (state) {
                case START:
                    writer.write("<!DOCTYPE ");
                    writer.write(first);
                    writer.write(" ");
                    writer.write(second);
                    writer.write(" \"");
                    writer.write(third);
                    writer.write("\" \"");
                    writer.write(fourth);
                    writer.write("\">\n");

                    break;

                default:
                    throw new IllegalStateException("XmlBuffer state " + state);
            }

            state = CLOSE;
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public void writeNoHtmlEscape(String text) {
        try {
            switch (state) {
                case OPEN:
                    writer.write(">");
                case START:
                case CLOSE:
                case TEXT:
                    if (text != null) {
                        writer.write(text);
                    }

                    break;

                default:
                    throw new IllegalStateException("XmlBuffer state " + state);
            }

            state = TEXT;
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public String toString() {
        String result = writer.toString();
        if (tagStack != null && !tagStack.empty()) {
            throw new IllegalStateException("Stack not empty: " + result);
        }
        return result;
    }

    public String getXml() {
        return toString();
    }

    public String escape(String s) {
        return StringEscapeUtils.escapeXml(s);
    }

    public String[] getAllowedEmptyTags() {
        return allowedEmptyTags;
    }

    public void setAllowedEmptyTags(String[] allowedEmptyTags) {
        this.allowedEmptyTags = allowedEmptyTags;
    }
}

