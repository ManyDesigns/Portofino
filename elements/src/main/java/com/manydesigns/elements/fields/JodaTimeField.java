/*
 * Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class JodaTimeField extends AbstractDateField<DateTime> {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public JodaTimeField(PropertyAccessor accessor, Mode mode) {
        this(accessor, mode, null);
    }

    public JodaTimeField(PropertyAccessor accessor, Mode mode, String prefix) {
        super(accessor, mode, prefix);
    }

    @Override
    protected DateTime toDate(@NotNull Number millisSince1970) {
        return new DateTime(millisSince1970.longValue());
    }

    @Override
    protected DateTime toDate(@NotNull DateTime dateTime) {
        return dateTime;
    }

    @Override
    protected DateTime fromDate(@NotNull DateTime dateValue) {
        return dateValue;
    }

}
