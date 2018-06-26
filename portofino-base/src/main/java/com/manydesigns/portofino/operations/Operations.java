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

package com.manydesigns.portofino.operations;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.portofino.buttons.GuardType;
import com.manydesigns.portofino.buttons.annotations.Guard;
import com.manydesigns.portofino.buttons.annotations.Guards;
import ognl.OgnlContext;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Operations {
    public static final String copyright =
            "Copyright (C) 2005-2017 ManyDesigns srl";

    public static List<Operation> getOperations(Class<?> someClass) {
        try {
            return operations.get(someClass);
        } catch (ExecutionException e) {
            throw new Error(e);
        }
    }

    public static List<Operation> computeOperationsForClass(Class<?> someClass) {
        List<Operation> operations = new ArrayList<>();
        for(Method method : someClass.getMethods()) {
            if(method.isBridge() || method.isSynthetic()) {
                continue;
            }
            Operation operation = getOperation(method);
            if(operation != null) {
                operations.add(operation);
            }
        }
        return operations;
    }

    protected static LoadingCache<Class, List<Operation>> operations =
            CacheBuilder
                    .newBuilder()
                    .maximumSize(1000)
                    .build(new CacheLoader<Class, List<Operation>>() {
                        @Override
                        public List<Operation> load(Class key) throws Exception {
                            return computeOperationsForClass(key);
                        }
                    });

    public static Operation getOperation(Method method) {
        String path = "";
        Path pathAnn = method.getAnnotation(Path.class);
        if(pathAnn != null) {
            path = pathAnn.value();
        }
        Annotation annotation = method.getAnnotation(GET.class);
        if(annotation == null) {
            annotation = method.getAnnotation(POST.class);
        }
        if(annotation == null) {
            annotation = method.getAnnotation(PUT.class);
        }
        if(annotation == null) {
            annotation = method.getAnnotation(DELETE.class);
        }
        if(annotation == null) {
            return null;
        }
        StringBuilder signature = new StringBuilder(annotation.annotationType().getSimpleName() + " " + path);
        String paramSeparator = "?";
        for(Annotation[] paramAnns : method.getParameterAnnotations()) {
            for(Annotation paramAnn : paramAnns) {
                if(paramAnn instanceof QueryParam) {
                    signature.append(paramSeparator).append(((QueryParam) paramAnn).value());
                    paramSeparator = "&";
                }
            }
        }
        return new Operation(method, signature.toString().trim());
    }

    public static boolean doGuardsPass(Object actionBean, Method method) {
        return doGuardsPass(actionBean, method, null);
    }

    public static boolean doGuardsPass(Object actionBean, Method method, @Nullable GuardType type) {
        List<Guard> guards = getGuards(method, type);
        boolean pass = true;
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        for(Guard guard : guards) {
            Object result = OgnlUtils.getValueQuietly(guard.test(), ognlContext, actionBean);
            pass &= result instanceof Boolean && ((Boolean) result);
        }
        return pass;
    }

    public static List<Guard> getGuards(Method method, GuardType type) {
        List<Guard> guardList = new ArrayList<Guard>();
        Guard guard = method.getAnnotation(Guard.class);
        if(guard != null && (type == null || type == guard.type())) {
            guardList.add(guard);
        } else {
            Guards guards = method.getAnnotation(Guards.class);
            if(guards != null) {
                for(Guard g : guards.value()) {
                    if(type == null || type == g.type()) {
                        guardList.add(g);
                    }
                }
            }
        }
        return guardList;
    }

}
