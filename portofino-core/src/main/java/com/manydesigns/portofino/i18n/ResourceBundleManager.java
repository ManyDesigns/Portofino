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

package com.manydesigns.portofino.i18n;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ResourceBundleManager {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";
    public static final String DEFAULT_BUNDLE_NAME = "portofino-messages";

    protected final File directory;
    protected final ConcurrentMap<Locale, ConfigurationResourceBundle> resourceBundles =
            new ConcurrentHashMap<Locale, ConfigurationResourceBundle>();

    private static final Logger logger = LoggerFactory.getLogger(ResourceBundleManager.class);
    protected final String resourceBundleName;

    public ResourceBundleManager(File dir) {
        this(dir, DEFAULT_BUNDLE_NAME);
    }

    public ResourceBundleManager(File dir, String resourceBundleName) {
        this.directory = dir;
        this.resourceBundleName = resourceBundleName;
    }

    protected String getBundleFileName(String baseName, Locale locale) {
        return getBundleName(baseName, locale) + ".properties";
    }

    protected String getBundleName(String baseName, Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();

        if (StringUtils.isBlank(language) && StringUtils.isBlank(country) && StringUtils.isBlank(variant)) {
            return baseName;
        }

        String name = baseName + "_";
        if (!StringUtils.isBlank(variant)) {
            name += language + "_" + country + "_" + variant;
        } else if (!StringUtils.isBlank(country)) {
            name += language + "_" + country;
        } else {
            name += language;
        }
        return name;
    }

    public ResourceBundle getBundle(Locale locale) {
        ConfigurationResourceBundle bundle = resourceBundles.get(locale);
        if(bundle == null) {
            ResourceBundle parentBundle = ResourceBundle.getBundle(resourceBundleName, locale);
            PropertiesConfiguration configuration;
            try {
                File bundleFile = getBundleFile(locale);
                if(!bundleFile.exists() && !locale.equals(parentBundle.getLocale())) {
                    bundleFile = getBundleFile(parentBundle.getLocale());
                }
                if(!bundleFile.exists()) {
                    return parentBundle;
                }
                configuration = new PropertiesConfiguration(bundleFile);
                FileChangedReloadingStrategy reloadingStrategy = new FileChangedReloadingStrategy();
                configuration.setReloadingStrategy(reloadingStrategy);
                bundle = new ConfigurationResourceBundle(configuration);
                bundle.setParent(parentBundle);
                resourceBundles.put(locale, bundle);
            } catch (ConfigurationException e) {
                logger.warn("Couldn't load app resource bundle for locale " + locale, e);
                return parentBundle;
            }
        }
        return bundle;
    }

    protected File getBundleFile(Locale locale) {
        String resourceName = getBundleFileName(resourceBundleName, locale);
        return new File(directory, resourceName);
    }
}
