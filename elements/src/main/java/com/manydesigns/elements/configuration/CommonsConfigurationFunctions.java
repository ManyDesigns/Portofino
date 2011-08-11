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
            "Copyright (c) 2005-2011, ManyDesigns srl";
    
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
