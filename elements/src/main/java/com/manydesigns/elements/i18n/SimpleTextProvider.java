/*
 * Copyright (C) 2005-2017 ManyDesigns srl.  All rights reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class SimpleTextProvider implements TextProvider {
    public static final String DEFAULT_MESSAGE_RESOURCE =
            "com.manydesigns.elements.messages";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(SimpleTextProvider.class);

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
        } catch (Exception e) {
            logger.warn("Couldn't load bundle: "+e.getMessage());
            tmpBundle  = null;
        }
        resourceBundle = tmpBundle;
    }

    //--------------------------------------------------------------------------
    // TextProvider implementation
    //--------------------------------------------------------------------------

    public String getText(String key, Object... args) {
        String localizedString = getLocalizedString(key);
        return MessageFormat.format(localizedString != null ? localizedString : key, args);
    }

    public String getTextOrNull(String key, Object... args) {
        String localizedString = getLocalizedString(key);
        return localizedString != null ? MessageFormat.format(localizedString, args) : null;
    }

    //--------------------------------------------------------------------------
    // Utility methods
    //--------------------------------------------------------------------------

    public String getLocalizedString(String key) {
        try{
            if (resourceBundle == null) {
                return null;
            } else {
                return resourceBundle.getString(key);
            }
        } catch (Exception e) {
            logger.debug("Key not found: " + key, e);
            return null;
        }
    }
}
