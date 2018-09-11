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

package com.manydesigns.portofino.code;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class JavaCodeBase implements CodeBase {
    
    //TODO reset()

    protected FileObject root;
    protected CodeBase parent;
    protected ClassLoader classLoader;
    
    protected JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    protected DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
    protected InMemoryFileManager fileManager =
            new InMemoryFileManager(compiler.getStandardFileManager(diagnosticCollector, null, null));
    protected VFSClassloader vfsClassloader = new VFSClassloader(fileManager.directory, getClassLoader());

    protected static final Logger logger = LoggerFactory.getLogger(JavaCodeBase.class);

    public JavaCodeBase(FileObject root) throws IOException {
        this.root = root;
    }

    public JavaCodeBase(FileObject root, CodeBase parent, ClassLoader classLoader) throws IOException {
        this.root = root;
        this.parent = parent;
        this.classLoader = classLoader;
    }

    public JavaCodeBase(FileObject root, CodeBase parent) throws IOException {
        this(root, parent, parent != null ? parent.getClassLoader() : null);
    }
    
    @Override
    public Class loadClass(String className) throws IOException, ClassNotFoundException {
        String resourceName = classNameToPath(className);
        FileObject fileObject = root.resolveFile(resourceName + ".class");
        if(fileObject.exists()) {
            return loadClassFile(fileObject, className);
        }
        fileObject = root.resolveFile(resourceName + ".java");
        if(fileObject.exists()) {
            return loadJavaFile(fileObject, className);
        }
        if(parent != null) {
            return parent.loadClass(className);
        }
        throw new ClassNotFoundException(className);
    }

    public Class loadClassFile(FileObject location, String name) throws FileSystemException, ClassNotFoundException {
        return new VFSClassloader(location, getClassLoader()).loadClass(name);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
    }

    public Class loadJavaFile(final FileObject fileObject, final String name) throws ClassNotFoundException {
        try {
            JavaFileObject javaFile = new VFSJavaFileObject(JavaFileObject.Kind.SOURCE, fileObject, name);
            JavaCompiler.CompilationTask task =
                    compiler.getTask(null, fileManager, null, null, null, Collections.singletonList(javaFile));
            if(task.call()) {
                return vfsClassloader.loadClass(name);
            } else {
                logger.warn("Compilation errors");
                //TODO log compilation errors
                throw new ClassNotFoundException(name);
            }
        } catch (Exception e) {
            throw new ClassNotFoundException(name, e);
        }
    }
    
    protected void listClassFiles(String packageName, List<JavaFileObject> list) throws IOException {
        Enumeration<URL> resources = getClassLoader().getResources(packageName.replace('.', '/'));
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            FileObject fileObject = VFS.getManager().resolveFile(url.toString());
            if(fileObject.exists() && fileObject.getType() == FileType.FOLDER) {
                for(FileObject child : fileObject.getChildren()) {
                    if(child.getType() == FileType.FILE && "class".equalsIgnoreCase(child.getName().getExtension())) {
                        try {
                            String binaryName = FilenameUtils.removeExtension(child.getName().getPath()).replace(File.separatorChar, '.').replace('/', '.');
                            if (binaryName.startsWith(".")) {
                                binaryName = binaryName.substring(1);
                            }
                            list.add(new VFSJavaFileObject(JavaFileObject.Kind.CLASS, child, binaryName));
                        } catch (URISyntaxException e) {
                            throw new IOException(e);
                        }
                    }
                }
            }
        }
    }
    
    //TODO what about mixing different codebases?
    protected void listJavaFiles(String packageName, List<JavaFileObject> list) throws IOException {
        FileObject pkgFile = root.resolveFile(classNameToPath(packageName));
        if(pkgFile.exists() && pkgFile.getType() == FileType.FOLDER) {
            for(FileObject child : pkgFile.getChildren()) {
                if(child.getType() == FileType.FILE && "java".equalsIgnoreCase(child.getName().getExtension())) {
                    try {
                        String className = FilenameUtils.removeExtension(child.getName().getBaseName());
                        String binaryName = packageName + "." + className;
                        list.add(new VFSJavaFileObject(JavaFileObject.Kind.SOURCE, child, binaryName));
                    } catch (URISyntaxException e) {
                        throw new IOException(e);
                    }
                }
            }
        }
        if(parent instanceof JavaCodeBase) {
            ((JavaCodeBase) parent).listJavaFiles(packageName, list);
        }
    }

    public static class VFSClassloader extends ClassLoader {
        
        protected final FileObject fileObject;

        public VFSClassloader(FileObject fileObject) {
            this(fileObject, Thread.currentThread().getContextClassLoader());
        }
        
        public VFSClassloader(FileObject fileObject, ClassLoader parent) {
            super(parent);
            this.fileObject = fileObject;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            InputStream inputStream = null;
            try {
                if(fileObject.getType() == FileType.FILE) {
                    inputStream = fileObject.getContent().getInputStream();
                } else {
                    inputStream = fileObject.resolveFile(classNameToPath(name) + ".class").getContent().getInputStream();
                }
                byte[] code = IOUtils.toByteArray(inputStream);
                return defineClass(name, code);
            } catch (Exception e) {
                throw new ClassNotFoundException(name, e);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        public Class<?> defineClass(String name, byte[] code) {
            return defineClass(null, code, 0, code.length);
        }
    }

    public static String classNameToPath(String name) {
        return name.replace('.', FileName.SEPARATOR_CHAR);
    }

    public static class VFSJavaFileObject extends SimpleJavaFileObject {

        protected final FileObject source;
        protected final String binaryName;
        
        public VFSJavaFileObject(URI uri, Kind kind, FileObject source, String binaryName) {
            super(uri, kind);
            this.source = source;
            this.binaryName = binaryName;
        }
        
        public VFSJavaFileObject(Kind kind, FileObject source, String binaryName) throws URISyntaxException {
            super(sanitizeUri(new URI(source.getName().getURI())), kind);
            this.source = source;
            this.binaryName = binaryName;
        }
        
        @Override
        public InputStream openInputStream() throws IOException {
            return source.getContent().getInputStream();
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            InputStream input = openInputStream();
            try {
                return IOUtils.toString(input);
            } finally {
                IOUtils.closeQuietly(input);
            }
        }

        public FileObject getSource() {
            return source;
        }

        public String getBinaryName() {
            return binaryName;
        }
    }

    //TODO see http://atamur.blogspot.it/2009/10/using-built-in-javacompiler-with-custom.html to integrate with parent classloader
    public class InMemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        public final FileObject directory;

        /**
         * Creates a new instance of ForwardingJavaFileManager.
         * @param fileManager delegate to this file manager
         */
        public InMemoryFileManager(JavaFileManager fileManager) throws IOException {
            super(fileManager);
            FileSystemManager fsManager = VFS.getManager();
            directory = fsManager.resolveFile("ram://" + UUID.randomUUID().toString());
        }

        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind)
                throws IOException {
            if(kind == JavaFileObject.Kind.SOURCE) {
                FileObject source = root.resolveFile(classNameToPath(className) + ".java");
                return new VFSJavaFileObject(URI.create(source.getPublicURIString()), kind, source, className);
            } else {
                return super.getJavaFileForInput(location, className, kind);
            }
        }

        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            if(file instanceof VFSJavaFileObject) {
                String binaryName = ((VFSJavaFileObject) file).getBinaryName();
                if(binaryName != null) {
                    return binaryName;
                }
            }
            return super.inferBinaryName(location, file);
        }

        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
            //See http://atamur.blogspot.it/2009/10/using-built-in-javacompiler-with-custom.html
            List<JavaFileObject> list = new ArrayList<>();
            Iterable<JavaFileObject> superList = super.list(location, packageName, kinds, recurse);
            for(JavaFileObject fileObject : superList) {
                list.add(fileObject);
            }
            if(kinds.contains(JavaFileObject.Kind.SOURCE)) {
                listJavaFiles(packageName, list);
            }
            if(location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
                listClassFiles(packageName, list);
            }
            return list;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling) throws IOException {
            return new CompiledClass(super.getJavaFileForOutput(location, className, kind, sibling), className);
        }

        @Override
        public void close() throws IOException {
            super.close();
            directory.close();
        }

        public class CompiledClass extends ForwardingJavaFileObject<JavaFileObject> {
            public final String name;

            public CompiledClass(JavaFileObject fileObject, String name) {
                super(fileObject);
                this.name = name;
            }

            @Override
            public OutputStream openOutputStream() throws IOException {
                FileObject fileObject = directory.resolveFile(classNameToPath(name) + ".class");
                if(!fileObject.exists()) {
                    fileObject.createFile();
                    //TODO if it exists it is a recompilation and the vfsClassLoader should be re-created
                }
                return fileObject.getContent().getOutputStream();
            }

        }
        
    }

    protected static URI sanitizeUri(URI uri) throws URISyntaxException {
        if(uri.getPath() == null) {
            //URIs with a null path are not accepted, but resources in JAR files have such
            //URIs as jar:file:///foo.jar!com/foo/Example.class and the whole file:// URL
            //becomes the scheme specific part of the URI
            String schemeSpecificPart = uri.getSchemeSpecificPart();
            int lastIndexOf = schemeSpecificPart.lastIndexOf('!');
            if(lastIndexOf > 0) {
                uri = new URI(uri.getScheme(), uri.getHost(), schemeSpecificPart.substring(lastIndexOf + 1), uri.getFragment());
            }
        }
        return uri;
    }

    @Override
    public void close() {
        if (fileManager != null) try {
            fileManager.close();
        } catch (IOException e) {
            logger.warn("Could not close file manager", e);
        }
    }

    @Override
    public FileObject getRoot() {
        return root;
    }
}
