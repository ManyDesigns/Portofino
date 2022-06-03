package com.manydesigns.portofino.microservices.boot;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.apache.commons.vfs2.provider.local.LocalFileName;
import org.apache.commons.vfs2.provider.local.LocalFileSystem;
import org.apache.commons.vfs2.provider.ram.RamFileSystem;
import org.apache.commons.vfs2.provider.res.ResourceFileNameParser;
import org.apache.commons.vfs2.provider.res.ResourceFileProvider;
import org.apache.commons.vfs2.provider.res.ResourceFileSystemConfigBuilder;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringBootResourceFileProvider extends ResourceFileProvider {

    @Override
    public FileObject findFile(final FileObject baseFile, final String uri, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        final FileName fileName;
        if (baseFile != null) {
            fileName = parseUri(baseFile.getName(), uri);
        }
        else {
            fileName = parseUri(null, uri);
        }
        final String resourceName = fileName.getPath();

        ClassLoader classLoader = ResourceFileSystemConfigBuilder.getInstance().getClassLoader(fileSystemOptions);
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        FileSystemException.requireNonNull(classLoader, "vfs.provider.url/badly-formed-uri.error", uri);
        final URL url = classLoader.getResource(resourceName);

        if (url != null) {
            return getContext().getFileSystemManager().resolveFile(fixNestedURI(url.toExternalForm()));
        } else {
            return noFile(resourceName);
        }
    }

    private FileObject noFile(String name) {
        AbstractFileName fileName = new AbstractFileName("res", name, FileType.IMAGINARY) {
            @Override
            public FileName createName(String absolutePath, FileType fileType) {
                return null;
            }

            @Override
            protected void appendRootUri(StringBuilder buffer, boolean addPassword) {

            }
        };
        return new AbstractFileObject<RamFileSystem>(fileName, new RamFileSystem(fileName, new FileSystemOptions()) {}) {
            @Override
            protected long doGetContentSize() throws Exception {
                return 0;
            }

            @Override
            protected FileType doGetType() throws Exception {
                return FileType.IMAGINARY;
            }

            @Override
            protected String[] doListChildren() throws Exception {
                return new String[0];
            }
        };
    }

    //See https://github.com/bedatadriven/renjin/blob/cac412d232ad66d4ee8e37cfc8cb70a45e676e19/core/src/main/java/org/renjin/util/ClasspathFileProvider.java#L88-L126
    public String fixNestedURI(String uri) {
        int bang = uri.indexOf('!');
        if(bang < 0 || !uri.startsWith("jar:file:")) {
            return uri;
        } else {
            StringBuilder prefix = new StringBuilder();
            Matcher matcher = Pattern.compile("[.]jar!").matcher(uri);
            if(matcher.find()) {
                while (matcher.find()) {
                    prefix.append("jar:");
                }
            }
            matcher = Pattern.compile("([^.][^j][^a][^r])!").matcher(uri);
            return prefix + matcher.replaceAll("$1");
        }
    }
}
