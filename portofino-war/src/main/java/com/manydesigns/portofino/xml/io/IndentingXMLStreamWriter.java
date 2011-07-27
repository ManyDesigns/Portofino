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

package com.manydesigns.portofino.xml.io;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.NamespaceContext;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class IndentingXMLStreamWriter implements XMLStreamWriter {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static String INDENTATION_4_SPACES = "    ";

    private final XMLStreamWriter xmlStreamWriter;
    protected int indentation;
    protected boolean charachtersWritten;
    protected String indentationString = INDENTATION_4_SPACES;

    public IndentingXMLStreamWriter(XMLStreamWriter xmlStreamWriter) {
        this.xmlStreamWriter = xmlStreamWriter;
        charachtersWritten = false;
    }

    public void writeIndentation() throws XMLStreamException {
        if (charachtersWritten) {
            charachtersWritten = false;
            return;
        }
        xmlStreamWriter.writeCharacters("\n");
        for (int i = 0; i < indentation; i++) {
            xmlStreamWriter.writeCharacters(indentationString);
        }
    }

    public String getIndentationString() {
        return indentationString;
    }

    public void setIndentationString(String indentationString) {
        this.indentationString = indentationString;
    }

    //--------------------------------------------------------------------------
    // XMLStreamWriter implementation/delegation
    //--------------------------------------------------------------------------

    public void writeStartElement(String s) throws XMLStreamException {
        writeIndentation();
        indentation++;
        charachtersWritten = false;
        xmlStreamWriter.writeStartElement(s);
    }

    public void writeStartElement(String s, String s1) throws XMLStreamException {
        writeIndentation();
        indentation++;
        charachtersWritten = false;
        xmlStreamWriter.writeStartElement(s, s1);
    }

    public void writeStartElement(String s, String s1, String s2) throws XMLStreamException {
        writeIndentation();
        indentation++;
        charachtersWritten = false;
        xmlStreamWriter.writeStartElement(s, s1, s2);
    }

    public void writeEmptyElement(String s, String s1) throws XMLStreamException {
        writeIndentation();
        charachtersWritten = false;
        xmlStreamWriter.writeEmptyElement(s, s1);
    }

    public void writeEmptyElement(String s, String s1, String s2) throws XMLStreamException {
        writeIndentation();
        charachtersWritten = false;
        xmlStreamWriter.writeEmptyElement(s, s1, s2);
    }

    public void writeEmptyElement(String s) throws XMLStreamException {
        writeIndentation();
        charachtersWritten = false;
        xmlStreamWriter.writeEmptyElement(s);
    }

    public void writeEndElement() throws XMLStreamException {
        indentation--;
        writeIndentation();
        xmlStreamWriter.writeEndElement();
    }

    public void writeEndDocument() throws XMLStreamException {
        xmlStreamWriter.writeEndDocument();
    }

    public void close() throws XMLStreamException {
        xmlStreamWriter.close();
    }

    public void flush() throws XMLStreamException {
        xmlStreamWriter.flush();
    }

    public void writeAttribute(String s, String s1) throws XMLStreamException {
        xmlStreamWriter.writeAttribute(s, s1);
    }

    public void writeAttribute(String s, String s1, String s2, String s3) throws XMLStreamException {
        xmlStreamWriter.writeAttribute(s, s1, s2, s3);
    }

    public void writeAttribute(String s, String s1, String s2) throws XMLStreamException {
        xmlStreamWriter.writeAttribute(s, s1, s2);
    }

    public void writeNamespace(String s, String s1) throws XMLStreamException {
        xmlStreamWriter.writeNamespace(s, s1);
    }

    public void writeDefaultNamespace(String s) throws XMLStreamException {
        xmlStreamWriter.writeDefaultNamespace(s);
    }

    public void writeComment(String s) throws XMLStreamException {
        xmlStreamWriter.writeComment(s);
    }

    public void writeProcessingInstruction(String s) throws XMLStreamException {
        xmlStreamWriter.writeProcessingInstruction(s);
    }

    public void writeProcessingInstruction(String s, String s1) throws XMLStreamException {
        xmlStreamWriter.writeProcessingInstruction(s, s1);
    }

    public void writeCData(String s) throws XMLStreamException {
        xmlStreamWriter.writeCData(s);
    }

    public void writeDTD(String s) throws XMLStreamException {
        xmlStreamWriter.writeDTD(s);
    }

    public void writeEntityRef(String s) throws XMLStreamException {
        xmlStreamWriter.writeEntityRef(s);
    }

    public void writeStartDocument() throws XMLStreamException {
        xmlStreamWriter.writeStartDocument();
    }

    public void writeStartDocument(String s) throws XMLStreamException {
        xmlStreamWriter.writeStartDocument(s);
    }

    public void writeStartDocument(String s, String s1) throws XMLStreamException {
        xmlStreamWriter.writeStartDocument(s, s1);
    }

    public void writeCharacters(String s) throws XMLStreamException {
        xmlStreamWriter.writeCharacters(s);
        charachtersWritten = true;
    }

    public void writeCharacters(char[] chars, int i, int i1) throws XMLStreamException {
        xmlStreamWriter.writeCharacters(chars, i, i1);
    }

    public String getPrefix(String s) throws XMLStreamException {
        return xmlStreamWriter.getPrefix(s);
    }

    public void setPrefix(String s, String s1) throws XMLStreamException {
        xmlStreamWriter.setPrefix(s, s1);
    }

    public void setDefaultNamespace(String s) throws XMLStreamException {
        xmlStreamWriter.setDefaultNamespace(s);
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        xmlStreamWriter.setNamespaceContext(namespaceContext);
    }

    public NamespaceContext getNamespaceContext() {
        return xmlStreamWriter.getNamespaceContext();
    }

    public Object getProperty(String s) throws IllegalArgumentException {
        return xmlStreamWriter.getProperty(s);
    }
}
