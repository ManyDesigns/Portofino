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

package com.manydesigns.elements.configuration;

import org.apache.commons.configuration.Configuration;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class CommonsConfigurationFunctions {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";
    
    public static String getString(Configuration configuration, String key) {
        return configuration.getString(key);
    }
    
    public static int getInt(Configuration configuration, String key) {
        return configuration.getInt(key);
    }
    
    public static String[] getStringArray(Configuration configuration, String key) {
        return configuration.getStringArray(key);
    }
    
    public static BigDecimal getBigDecimal(Configuration configuration, String key) {
        return configuration.getBigDecimal(key);
    }
    
    public static BigInteger getBigInteger(Configuration configuration, String key) {
        return configuration.getBigInteger(key);
    }
    
    public static boolean getBoolean(Configuration configuration, String key) {
        return configuration.getBoolean(key);
    }
    
    public static byte getByte(Configuration configuration, String key) {
        return configuration.getByte(key);
    }
    
    public static double getDouble(Configuration configuration, String key) {
        return configuration.getDouble(key);
    }

    public static boolean containsKey(Configuration configuration, String key) {
        return configuration.containsKey(key);
    }

    public static float getFloat(Configuration configuration, String key) {
        return configuration.getFloat(key);
    }

    public static long getLong(Configuration configuration, String key) {
        return configuration.getLong(key);
    }

    public static short getShort(Configuration configuration, String key) {
        return configuration.getShort(key);
    }
}
