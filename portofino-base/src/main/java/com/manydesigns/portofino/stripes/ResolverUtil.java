package com.manydesigns.portofino.stripes;

import net.sourceforge.stripes.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ResolverUtil<T> extends net.sourceforge.stripes.util.ResolverUtil<T> {

    public static final Logger logger = LoggerFactory.getLogger(ResolverUtil.class);

    protected String[] extensions = { ".class" };

    @Override
    public ResolverUtil<T> find(Test test, String packageName) {
        String path = getPackagePath(packageName);

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader classLoader = getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            logger.debug("Listing path {} using classloader {}", path, classLoader);
            List<String> children = VFS.getInstance().list(path);
            logger.debug("Found: {}", children);
            for (String child : children) {
                for(String extension : extensions) {
                    if (child.endsWith(extension)) {
                        addIfMatching(test, child);
                        break;
                    }
                }
            }
        } catch (IOException ioe) {
            logger.warn("Could not read package: " + packageName + " -- " + ioe);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        return this;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public void setExtensions(String... extensions) {
        this.extensions = extensions;
    }
}
