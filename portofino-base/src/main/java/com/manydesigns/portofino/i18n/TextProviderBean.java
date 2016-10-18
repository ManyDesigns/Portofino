package com.manydesigns.portofino.i18n;

import com.manydesigns.elements.i18n.TextProvider;

/**
 * Utility class to expose the Elements text provider to the OGNL context.
 */
public class TextProviderBean {

    protected final TextProvider textProvider;

    public TextProviderBean(TextProvider textProvider) {
        this.textProvider = textProvider;
    }

    public String getText(String key) {
        return textProvider.getText(key);
    }

    public String getText(String key, Object arg0) {
        return textProvider.getText(key, arg0);
    }

    public String getText(String key, Object arg0, Object arg1) {
        return textProvider.getText(key, arg0, arg1);
    }

}
