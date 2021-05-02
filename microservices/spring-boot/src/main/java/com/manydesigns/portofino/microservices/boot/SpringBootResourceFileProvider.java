package com.manydesigns.portofino.microservices.boot;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.apache.commons.vfs2.provider.res.ResourceFileNameParser;
import org.apache.commons.vfs2.provider.res.ResourceFileProvider;
import org.apache.commons.vfs2.provider.res.ResourceFileSystemConfigBuilder;

import java.net.URL;

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

        FileSystemException.requireNonNull(url, "vfs.provider.url/badly-formed-uri.error", uri);

        return getContext().getFileSystemManager().resolveFile(fixNestedURI(url.toExternalForm()));
    }

    //See https://github.com/bedatadriven/renjin/blob/cac412d232ad66d4ee8e37cfc8cb70a45e676e19/core/src/main/java/org/renjin/util/ClasspathFileProvider.java#L88-L126
    public String fixNestedURI(String uri) {
        int bang = uri.indexOf('!');
        if(bang < 0 || !uri.startsWith("jar:file:")) {
            return uri;
        } else {
            StringBuilder prefix = new StringBuilder();
            for(int i = bang + 1; i < uri.length(); i++) {
                if(uri.charAt(i) == '!') {
                    prefix.append("jar:");
                }
            }
            return prefix + uri;
        }
    }
}
