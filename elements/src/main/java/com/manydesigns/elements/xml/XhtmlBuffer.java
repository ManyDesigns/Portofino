/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;

/**
 * @author Paolo Predonzani - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo      - angelo.lupo@manydesigns.com
 */
public class XhtmlBuffer extends XmlBuffer implements XhtmlFragment {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static String[] XHTML_EMPTY_TAG_ALLOWED_LIST = {
            "area",
            "base",
            "br",
            "col",
            "hr",
            "img",
            "input",
            "link",
            "meta",
            "param"
    };

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public XhtmlBuffer() {
        this(new StringWriter());
    }

    public XhtmlBuffer(Writer writer) {
        super(writer);
        allowedEmptyTags = XHTML_EMPTY_TAG_ALLOWED_LIST;
    }

    //~--- methods ------------------------------------------------------------

    public void writeAnchor(String href, String text) {
        writeAnchor(href, text, null, null);
    }

    public void writeNbsp() {
        writeNoHtmlEscape("&nbsp;");
    }

    public void writeAnchor(String href, String text,
                            String classStr, String title) {
        openElement("a");
        if (href != null) {
            addAttribute("href", href);
        }
        if (classStr != null) {
            addAttribute("class", classStr);
        }
        if (title != null) {
            addAttribute("title", title);
        }
        write(text);
        closeElement("a");
    }

    public void writeCaption(String text) {
        openElement("caption");
        write(text);
        closeElement("caption");
    }

    public void writeLegend(String text, @Nullable String htmlClass) {
        openElement("legend");
        addAttribute("class", htmlClass);
        write(text);
        closeElement("legend");
    }

    public void writeLabel(String text, String forId, String htmlClass) {
        openElement("label");
        if (forId != null) {
            addAttribute("for", forId);
        }

        addAttribute("class", htmlClass);
        write(text);
        closeElement("label");
    }

    public void writeBr() {
//        openElement("br");
//        closeElement("br");
        writeNoHtmlEscape("<br />");
    }

    public void writeDoctype() {
//        writeDoctype("html", "PUBLIC", "-//W3C//DTD XHTML 1.0 Transitional//EN",
//                     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
        writeDoctype("html", "PUBLIC", "-//W3C//DTD XHTML 1.0 Strict//EN",
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
    }

    public void writeH1(String title) {
        openElement("h1");
        write(title);
        closeElement("h1");
    }

    public void writeH2(String title) {
        openElement("h2");
        write(title);
        closeElement("h2");
    }

    public void writeHr() {
        openElement("hr");
        closeElement("hr");
    }

    public void writeInputCheckbox(@Nullable String id,
                                   @Nullable String name,
                                   @Nullable String value,
                                   boolean checked) {
        writeInputCheckbox(id, name, value, checked, false, null);
    }

    public void writeInputCheckbox(@Nullable String id,
                                   @Nullable String name,
                                   @Nullable String value,
                                   boolean checked,
                                   boolean disabed,
                                   @Nullable String cssClass) {
        openElement("input");
        addAttribute("id", id);
        addAttribute("type", "checkbox");
        addAttribute("name", name);
        addAttribute("value", value);

        if (checked) {
            addAttribute("checked", "checked");
        }

        if (disabed)
            addAttribute("disabled", "disabled");

        if(cssClass != null) {
            addAttribute("class", cssClass);
        }

        closeElement("input");
    }

    public void writeInputCheckbox(@Nullable String id,
                                       @Nullable String name,
                                       @Nullable String value,
                                       boolean checked,
                                       boolean disabed,
                                       @Nullable String cssClass,
                                       @Nullable String title) {
            openElement("input");
            addAttribute("id", id);
            addAttribute("type", "checkbox");
            addAttribute("name", name);
            addAttribute("value", value);

            if (checked) {
                addAttribute("checked", "checked");
            }

            if (disabed)
                addAttribute("disabled", "disabled");

            if(cssClass != null) {
                addAttribute("class", cssClass);
            }

            if(title != null) {
                addAttribute("title", title);
            }

            closeElement("input");
        }

    public void writeInputHidden(String name, String value) {
        openElement("input");
        addAttribute("type", "hidden");
        addAttribute("name", name);
        addAttribute("value", value);
        closeElement("input");
    }

    public void writeInputHidden(String id, String name, String value) {
        openElement("input");
        addAttribute("type", "hidden");
        addAttribute("id", id);
        addAttribute("name", name);
        addAttribute("value", value);
        closeElement("input");
    }

    public void writeInputRadio(@Nullable String id,
                                @Nullable String name,
                                @Nullable String value,
                                boolean checked) {
        writeInputRadio(id, name, value, checked, false, null);
    }

    public void writeInputRadio(@Nullable String id,
                                @Nullable String name,
                                @Nullable String value,
                                boolean checked,
                                boolean disabled) {
        writeInputRadio(id, name, value, checked, disabled, null);
    }

    public void writeInputRadio(@Nullable String id,
                                @Nullable String name,
                                @Nullable String value,
                                boolean checked,
                                boolean disabled,
                                @Nullable String onClickEvent) {
        openElement("input");
        addAttribute("type", "radio");
        addAttribute("id", id);
        addAttribute("name", name);
        addAttribute("value", value);

        if (checked) {
            addAttribute("checked", "checked");
        }

        if (disabled)
            addAttribute("disabled", "disabled");

        addAttribute("onclick", onClickEvent);
        closeElement("input");
    }

    public void writeInputSubmit(String name,
                                 String value,
                                 @Nullable String onSubmit) {
        openElement("input");
        addAttribute("type", "submit");
        addAttribute("name", name);
        addAttribute("value", value);
        addAttribute("class", "submit");
        addAttribute("onclick", onSubmit);
        closeElement("input");
    }

    public void writeInputText(@Nullable String id, @Nullable String name, String value,
                               String htmlClass, @Nullable Integer size,
                               @Nullable Integer maxLength) {
        writeInputText(id, name, value, null, htmlClass, size, maxLength);
    }

    public void writeInputText(@Nullable String id, @Nullable String name, String value,
                               @Nullable String placeholder, String htmlClass, @Nullable Integer size,
                               @Nullable Integer maxLength) {
        openElement("input");
        addAttribute("id", id);
        addAttribute("type", "text");
        addAttribute("name", name);
        addAttribute("value", value);
        if(placeholder != null) {
            addAttribute("placeholder", placeholder);
        }
        if (size != null) {
            addAttribute("size", Integer.toString(size));
            htmlClass = StringUtils.defaultString(htmlClass) + " mde-text-field-with-explicit-size";
        }
        addAttribute("class", htmlClass);
        if (maxLength != null) {
            addAttribute("maxlength", Integer.toString(maxLength));
        }
        closeElement("input");
    }

    public void writeInputNumber(@Nullable String id, @Nullable String name, String value,
                               @Nullable String placeholder, String htmlClass, @Nullable Integer size,
                               @Nullable Integer maxLength, @Nullable BigDecimal maxValue ,
                               @Nullable BigDecimal minValue , @Nullable Integer step) {
        openElement("input");
        addAttribute("id", id);
        addAttribute("type", "number");
        addAttribute("name", name);
        addAttribute("value", value);

        if (minValue != null) {
            addAttribute("min", minValue.toString());
        }
        if (maxValue != null) {
            addAttribute("max", maxValue.toString());
        }
        if (step != null) {
            addAttribute("step", Integer.toString(step));
        }
        if(placeholder != null) {
            addAttribute("placeholder", placeholder);
        }
        if (size != null) {
            addAttribute("size", Integer.toString(size));
            htmlClass = StringUtils.defaultString(htmlClass) + " mde-text-field-with-explicit-size";
        }
        addAttribute("class", htmlClass);
        if (maxLength != null) {
            addAttribute("maxlength", Integer.toString(maxLength));
        }
        closeElement("input");
    }

    public void writeOption(String value, boolean selected, String text) {
        openElement("option");
        addAttribute("value", value);

        if (selected) {
            addAttribute("selected", "selected");
        }

        write(text);
        closeElement("option");
    }

    public void writeParagraph(String value) {
        openElement("p");
        write(value);
        closeElement("p");
    }

    public void writeJavaScript(String script) {
        openElement("script");
        addAttribute("type", "text/javascript");
        write(script);
        closeElement("script");
    }

    public void writeInputFile(String id, String name, String value,
                               boolean disabled) {
        openElement("input");
        addAttribute("type", "file");
        addAttribute("id", id);
        addAttribute("name", name);
        addAttribute("value", value);
        addAttribute("class", "text file");
        addAttribute("data-show-preview", "false");
        addAttribute("data-show-upload", "false");
        if (disabled)
            addAttribute("disabled", "disabled");
        closeElement("input");
        String script = "$('#"+StringEscapeUtils.escapeJavaScript(id)+"').fileinput({'showUpload':false, 'previewFileType':'text' , 'browseLabel':'' , 'removeLabel':''}); ";

        if (disabled)
            script+="$('#"+StringEscapeUtils.escapeJavaScript(id)+"').fileinput('disable');";

         writeJavaScript(script);

    }

    public void writeInputFile(String id, String name, boolean disabled) {
        writeInputFile(id, name, null, disabled);
    }

    public void writeImage(String src, String alt, String title,
                           String id, String htmlClass) {
        openElement("img");
        addAttribute("src", src);
        addAttribute("alt", alt);
        addAttribute("class", htmlClass);
        addAttribute("id", id);
        addAttribute("title", title);
        closeElement("img");
    }

    public void writeLink(String rel, String type, String href) {
        openElement("link");
        addAttribute("rel", rel);
        addAttribute("type", type);
        addAttribute("href", href);
        closeElement("link");
    }

    public void writeStyle(String body) {
        openElement("style");
        write(body);
        closeElement("style");
    }

    public void openFormElement(String id, String method,
                                String action, String htmlClass) {
        openElement("form");
        addAttribute("id", id);
        addAttribute("method", method);
        addAttribute("action", action);
        addAttribute("class", htmlClass);
    }

    public void closeFormElement() {
        closeElement("form");
    }

    @Override
    public String escape(String s) {
        return StringEscapeUtils.escapeHtml(s);
    }


    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.write(this);
    }
}
