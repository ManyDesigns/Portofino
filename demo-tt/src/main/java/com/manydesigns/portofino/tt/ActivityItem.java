package com.manydesigns.portofino.tt;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import org.apache.commons.lang.time.FastDateFormat;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: predo
 * Date: 12/10/13
 * Time: 4:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActivityItem implements XhtmlFragment {

    FastDateFormat dateFormat;

    final Locale locale;
    final Date timestamp;
    final String imageSrc;
    final String imageHref;
    final String imageAlt;
    final String message;
    final String key;
    final Arg[] args;

    public ActivityItem(Locale locale, Date timestamp, String imageSrc, String imageHref, String imageAlt, String message, String key, Arg... args) {
        this.locale = locale;
        this.timestamp = timestamp;
        this.imageSrc = imageSrc;
        this.imageHref = imageHref;
        this.imageAlt = imageAlt;
        this.message = message;
        this.key = key;
        this.args = args;

        dateFormat = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, locale);
    }

    @Override
    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "media");

        renderImage(xb);

        renderBody(xb);

        xb.closeElement("div");
    }

    public void renderImage(XhtmlBuffer xb) {
        String absoluteSrc = Util.getAbsoluteUrl(imageSrc);
        if (imageHref == null) {
            xb.openElement("div");
        } else {
            xb.openElement("a");
            xb.addAttribute("href", imageHref);
        }
        xb.addAttribute("class", "pull-left");

        xb.openElement("img");
        xb.addAttribute("class", "media-object");
        xb.addAttribute("alt", imageAlt);
        xb.addAttribute("src", absoluteSrc);
        xb.closeElement("img");

        if (imageHref == null) {
            xb.closeElement("div");
        } else {
            xb.closeElement("a");
        }
    }

    public void renderBody(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "media-body");

        renderData(xb);
        renderTimestamp(xb);
        renderMessage(xb);

        xb.closeElement("div");
    }

    public void renderData(XhtmlBuffer xb) {

        List<String> formattedArgs = new ArrayList<String>(args.length);
        for (Arg arg : args) {
            String formattedArg = arg.format();
            formattedArgs.add(formattedArg);
        }

        String text = ElementsThreadLocals.getText(key, formattedArgs.toArray());

        xb.openElement("div");
        xb.writeNoHtmlEscape(text);
        xb.closeElement("div");
    }



    public void renderTimestamp(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.openElement("small");
        xb.addAttribute("class", "muted");
        xb.openElement("strong");
        xb.write(dateFormat.format(timestamp));
        xb.closeElement("strong");
        xb.closeElement("small");
        xb.closeElement("div");
    }

    public void renderMessage(XhtmlBuffer xb) {
        if (message != null) {
            xb.openElement("div");
            xb.write(message);
            xb.closeElement("div");
        }
    }

    static public class Arg {
        final String text;
        final String href;

        public Arg(String text, String href) {
            this.text = text;
            this.href = href;
        }

        public String getText() {
            return text;
        }

        public String getHref() {
            return href;
        }

        public String format() {
            XhtmlBuffer argXb = new XhtmlBuffer();
            argXb.openElement("strong");
            if (href == null) {
                argXb.write(text);
            } else {
                String absoluteHref = Util.getAbsoluteUrl(href);
                argXb.writeAnchor(absoluteHref, text);
            }
            argXb.closeElement("strong");
            return argXb.toString();
        }
    }
}
