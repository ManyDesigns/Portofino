package com.manydesigns.portofino.ui.support;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;

public class BaseHrefReplaceListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(BaseHrefReplaceListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String indexPath = sce.getServletContext().getRealPath("/index.html");
        File indexFile = new File(indexPath);
        if(indexFile.exists() && indexFile.canRead() && indexFile.canWrite()) {
            try(FileReader reader = new FileReader(indexFile)) {
                StringWriter fileContents = new StringWriter();
                IOUtils.copy(reader, fileContents);
                String newContents = fileContents.toString();
                String regex = "<base +href *= *['\"][^'\"]*['\"] */?>";
                String replacement = "<base href='" + sce.getServletContext().getContextPath() + "/'>";
                newContents = newContents.replaceFirst(regex, replacement);
                try(FileWriter writer = new FileWriter(indexFile)) {
                    writer.write(newContents);
                }
            } catch (IOException e) {
                logger.warn("Could not patch index.html with the correct base href", e);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
