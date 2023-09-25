/*
* Copyright (C) 2005-2022 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.tt

import com.fasterxml.jackson.annotation.JsonIgnore

/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.util.Util
import com.manydesigns.elements.xml.XhtmlBuffer
import com.manydesigns.elements.xml.XhtmlFragment
import org.apache.commons.lang.time.FastDateFormat
import org.jetbrains.annotations.NotNull

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
class ActivityItem implements XhtmlFragment {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    @JsonIgnore
    FastDateFormat dateFormat;

    @JsonIgnore
    final Locale locale;
    final long timestamp;
    final String imageSrc;
    final String imageHref;
    final String imageAlt;
    final String message;
    final String key;
    final List<Arg> args = new ArrayList<Arg>();

    boolean fullUrls = false;

    public ActivityItem(Locale locale, Date timestamp, String imageSrc, String imageHref, String imageAlt, String message, String key) {
        this.locale = locale
        this.timestamp = timestamp.time
        this.imageSrc = imageSrc
        this.imageHref = imageHref
        this.imageAlt = imageAlt
        this.message = message
        this.key = key

        dateFormat = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, locale)
    }

    @Override
    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "media");

        writeImage(xb);

        writeBody(xb);

        xb.closeElement("div");
    }

    public void writeImage(XhtmlBuffer xb) {
        String absoluteSrc = Util.getAbsoluteUrl(imageSrc, fullUrls);
        xb.openElement("div");
        xb.addAttribute("class", "media-left");
        if (imageHref != null) {
            xb.openElement("a");
            xb.addAttribute("href", imageHref);
        }
        xb.openElement("img");
        xb.addAttribute("class", "media-object");
        xb.addAttribute("alt", imageAlt);
        xb.addAttribute("src", absoluteSrc);
        xb.closeElement("img");

        if (imageHref != null) {
            xb.closeElement("a");
        }
        xb.closeElement("div");
    }

    public void writeBody(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "media-body");

        writeData(xb);
        writeTimestamp(xb);
        writeMessage(xb);

        xb.closeElement("div");
    }

    public void writeData(XhtmlBuffer xb) {

        List<String> formattedArgs = new ArrayList<String>(args.size());
        for (Arg arg : args) {
            String formattedArg = arg.format();
            formattedArgs.add(formattedArg);
        }

        String text = ElementsThreadLocals.getText(key, formattedArgs.toArray());

        xb.openElement("div");
        xb.writeNoHtmlEscape(text);
        xb.closeElement("div");
    }



    public void writeTimestamp(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.openElement("small");
        xb.addAttribute("class", "text-muted");
        xb.write(dateFormat.format(new Date(timestamp)));
        xb.closeElement("small");
        xb.closeElement("div");
    }

    public void writeMessage(XhtmlBuffer xb) {
        if (message != null) {
            xb.openElement("div");
            xb.addAttribute("class", "activity-item-message");
            xb.writeNoHtmlEscape(message);
            xb.closeElement("div");
        }
    }

    public boolean isFullUrls() {
        return fullUrls;
    }

    public void setFullUrls(boolean fullUrls) {
        this.fullUrls = fullUrls;
    }

    public void addArg(String text, String url) {
        Arg arg = new Arg(text, url);
        args.add(arg);
    }

    public List<Arg> getArgs() {
        return args;
    }

    public class Arg {
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
                String absoluteHref = Util.getAbsoluteUrl(href, fullUrls);
                argXb.writeAnchor(absoluteHref, text);
            }
            argXb.closeElement("strong");
            return argXb.toString();
        }
    }

}
