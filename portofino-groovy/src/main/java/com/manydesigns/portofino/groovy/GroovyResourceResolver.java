/*
 * Copyright (C) 2016 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.groovy;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.dispatcher.resolvers.AbstractResourceResolver;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class GroovyResourceResolver extends AbstractResourceResolver {

    protected final GroovyCodeBase groovyCodeBase;
    protected static final Logger logger = LoggerFactory.getLogger(GroovyResourceResolver.class);

    public GroovyResourceResolver() {
        this(getGroovyCodeBase(null));
    }

    public GroovyResourceResolver(GroovyCodeBase groovyCodeBase) {
        this.groovyCodeBase = groovyCodeBase;
    }
    
    public GroovyResourceResolver(CodeBase codeBase) {
            this(getGroovyCodeBase(codeBase));
        }
        
    protected static GroovyCodeBase getGroovyCodeBase(CodeBase codeBase) {
        GroovyCodeBase gcb = getGroovyCodeBaseInHierarchy(codeBase);
        if (gcb != null) {
            return gcb;
        }
        try {
            return new GroovyCodeBase(
                    VFS.getManager().resolveFile("ram:///" + GroovyResourceResolver.class.getName()),
                    codeBase);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static GroovyCodeBase getGroovyCodeBaseInHierarchy(CodeBase codeBase) {
        if (codeBase == null) {
            return null;
        } else if(codeBase instanceof GroovyCodeBase) {
            return (GroovyCodeBase) codeBase;
        } else {
            return getGroovyCodeBaseInHierarchy(codeBase.getParent());
        }
    }

    @Override
    public <T> T resolve(FileObject location, Class<T> type) throws Exception {
        FileObject actualLocation = resolve(location);
        if(actualLocation == null) {
            return null;
        }
        return type.cast(groovyCodeBase.loadGroovyFile(actualLocation));
    }
    
    @Override
    public boolean supports(Class<?> type) {
        return type == Class.class;
    }
    
    @Override
    public boolean supports(String extension) {
        return "groovy".equals(extension);
    }
}
