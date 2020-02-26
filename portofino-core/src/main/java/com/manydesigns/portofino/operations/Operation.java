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

package com.manydesigns.portofino.operations;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Alessio Stalla       - alessio.stalla@gmail.com
 */
public class Operation {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    private final Method method;
    private final String signature;
    private final List<String> parameters;

    public Operation(Method method, String signature, List<String> parameters) {
        this.method = method;
        this.signature = signature;
        this.parameters = parameters;
    }

    public Method getMethod() {
        return method;
    }

    public String getName() {
        return method.getName();
    }

    public String getSignature() {
        return signature;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
