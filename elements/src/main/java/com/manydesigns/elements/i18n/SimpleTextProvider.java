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

package com.manydesigns.elements.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class SimpleTextProvider implements TextProvider {
    private static final String DEFAULT_MESSAGE_RESOURCE =
            "com.manydesigns.elements.messages";

    protected final Locale locale;
    protected final String messageResource;
    protected final ResourceBundle resourceBundle;

    public SimpleTextProvider(Locale locale) {
        this(locale, DEFAULT_MESSAGE_RESOURCE);
    }

    public SimpleTextProvider(Locale locale, String messageResource) {
        this.locale = locale;
        this.messageResource = messageResource;

        ResourceBundle tmpBundle;
        try{
            if (locale == null)
                tmpBundle = ResourceBundle.getBundle(messageResource);
            else {
                tmpBundle = ResourceBundle.getBundle(messageResource, locale);
            }
        } catch (Throwable e) {
            tmpBundle  = null;
        }
        resourceBundle = tmpBundle;
    }

    public String getText(String key, Object... args) {
        String localizedString = getLocalizedString(key);
        return MessageFormat.format(localizedString, args);
    }

    public String getLocalizedString(String key) {
        try{
            if (resourceBundle == null) {
                return key;
            } else {
                return resourceBundle.getString(key);
            }
        } catch (Throwable e) {
            return key;
        }
    }
}
