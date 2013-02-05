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

package com.manydesigns.elements.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class SimpleTextProvider implements TextProvider {
    public static final String DEFAULT_MESSAGE_RESOURCE =
            "com.manydesigns.elements.messages";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected final Locale locale;
    protected final String messageResource;
    protected final ResourceBundle resourceBundle;

    //--------------------------------------------------------------------------
    // static builder methods
    //--------------------------------------------------------------------------

    public static SimpleTextProvider create() {
        return create(Locale.getDefault(), DEFAULT_MESSAGE_RESOURCE);
    }

    public static SimpleTextProvider create(Locale locale) {
        return create(locale, DEFAULT_MESSAGE_RESOURCE);
    }

    public static SimpleTextProvider create(String messageResource) {
        return create(Locale.getDefault(), messageResource);
    }

    public static SimpleTextProvider create(Locale locale, String messageResource) {
        return new SimpleTextProvider(locale, messageResource);
    }

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    private SimpleTextProvider(Locale locale, String messageResource) {
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

    //--------------------------------------------------------------------------
    // TextProvider implementation
    //--------------------------------------------------------------------------

    public String getText(String key, Object... args) {
        String localizedString = getLocalizedString(key);
        return MessageFormat.format(localizedString, args);
    }

    //--------------------------------------------------------------------------
    // Utility methods
    //--------------------------------------------------------------------------

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
