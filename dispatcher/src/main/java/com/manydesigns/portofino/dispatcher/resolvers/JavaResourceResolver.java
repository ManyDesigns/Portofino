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

package com.manydesigns.portofino.dispatcher.resolvers;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;

import java.io.IOException;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class JavaResourceResolver extends CachingResourceResolver {

    protected CodeBase codeBase;

    public JavaResourceResolver() {}
    
    public JavaResourceResolver(CodeBase codeBase) {
        this.codeBase = codeBase;
    }

    @Override
    protected <T> T doResolve(FileObject location, Class<T> type) throws Exception {
        FileObject actualLocation = resolve(location);
        if(actualLocation != null) {
            if("class".equals(actualLocation.getName().getExtension())) {
                return type.cast(resolveClassFile(actualLocation));
            } else if("java".equals(actualLocation.getName().getExtension())) {
                return type.cast(resolveJavaFile(actualLocation));
            }
        }
        return null;
    }
    
    public FileObject resolve(FileObject location) throws FileSystemException {
        return AbstractResourceResolver.resolve(this, location);
    }
    
    @Override
    public FileObject resolve(FileObject location, String name) throws FileSystemException {
        return AbstractResourceResolver.resolve(this, location, name);
    }
    
    @Override
    public boolean supports(Class<?> type) {
        return type == Class.class;
    }
    
    @Override
    public boolean supports(String extension) {
        return "java".equals(extension) || "class".equals(extension);
    }

    protected Class resolveClassFile(FileObject location) throws Exception {
        return new JavaCodeBase(location.getParent(), codeBase).
                loadClassFile(location, getBaseName(location));
    }

    public CodeBase getCodeBase() {
        return codeBase;
    }

    protected Class resolveJavaFile(final FileObject fileObject) throws ClassNotFoundException, IOException {
        try {
            JavaType<?> result = Roaster.parse(fileObject.getContent().getInputStream());
            String className = result.getQualifiedName();
            return new JavaCodeBase(fileObject.getParent(), codeBase).loadJavaFile(fileObject, className);
        } finally {
            fileObject.close();
        }
    }

    protected static String getBaseName(FileObject fileObject) {
        return FilenameUtils.getBaseName(fileObject.getName().getBaseName());
    }

}
