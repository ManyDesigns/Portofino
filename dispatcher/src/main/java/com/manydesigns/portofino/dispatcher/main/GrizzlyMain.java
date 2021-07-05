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

package com.manydesigns.portofino.dispatcher.main;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.GroovyCodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.dispatcher.Root;
import com.manydesigns.portofino.dispatcher.resolvers.CachingResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.GroovyResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.JavaResourceResolver;
import com.manydesigns.portofino.dispatcher.resolvers.ResourceResolvers;
import com.manydesigns.portofino.dispatcher.swagger.DocumentedApiRoot;
import com.manydesigns.portofino.dispatcher.web.ApplicationRoot;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

/**
 * Created by alessio on 4/28/16.
 */
public class GrizzlyMain {

    private static final Logger logger = LoggerFactory.getLogger(GrizzlyMain.class);
    
    public static void main(String[] args) throws Exception {
        File currentDirectory = new File("");
        FileSystemManager manager = VFS.getManager();
        if(manager instanceof DefaultFileSystemManager) {
            ((DefaultFileSystemManager) manager).setBaseFile(currentDirectory);
        }
        String rootPath = currentDirectory.getAbsolutePath();
        if(args.length > 0) {
            rootPath = args[0];
        }
        FileObject codeBaseRoot;
        try {
            codeBaseRoot = VFS.getManager().resolveFile("res:");
        } catch (Exception e) {
            codeBaseRoot = VFS.getManager().resolveFile("res:com/manydesigns/portofino/dispatcher/main/GrizzlyMain.class").
                    getParent().getParent().getParent().getParent().getParent().getParent();
        }
        CodeBase codeBase = new JavaCodeBase(codeBaseRoot);
        codeBase = new GroovyCodeBase(codeBaseRoot, codeBase);
        ResourceResolvers resourceResolver = new ResourceResolvers();
        resourceResolver.resourceResolvers.add(new JavaResourceResolver(codeBase));
        resourceResolver.resourceResolvers.add(new GroovyResourceResolver(codeBase));
        FileObject root = manager.resolveFile(rootPath);
        logger.info("Codebase rootFactory: " + codeBaseRoot.getURL());
        logger.info("Root path: " + root.getURL());
        DocumentedApiRoot.setRootFactory(() -> Root.get(root, resourceResolver));
        String host = System.getProperty("portofino.web.host", "0.0.0.0");
        String port = System.getProperty("portofino.web.port", "8090");
        ResourceConfig config = new ResourceConfig(ApplicationRoot.class, OpenApiResource.class);
        config.property(ServletProperties.FILTER_FORWARD_ON_404, true);
        try {
            config.register(Class.forName("org.glassfish.jersey.jackson.JacksonFeature"));
            ResourceResolver jacksonRR = (ResourceResolver) Class.forName("com.manydesigns.portofino.dispatcher.resolvers.JacksonResourceResolver").newInstance();
            resourceResolver.resourceResolvers.add(new CachingResourceResolver(jacksonRR));
        } catch (ClassNotFoundException e) {
            //Jackson not available
        }
        try {
            config.register(Class.forName("com.manydesigns.portofino.dispatcher.security.ShiroResourceFilter"));
        } catch (ClassNotFoundException e) {
            //Shiro not available
        }
        /* TODO
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost(host + ":" + port);
        beanConfig.setBasePath("/");
        beanConfig.setResourcePackage(ApplicationRoot.class.getPackage().getName());
        beanConfig.setScan(true);*/

        URI uri = URI.create("http://" + host + ":" + port + "/");
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, config);
        //https://scalajamsession.wordpress.com/2015/06/27/how-to-serve-static-files-with-jerseygrizzly-setup-and-control-access-to-static-content/
        ClassLoader loader = GrizzlyMain.class.getClassLoader();
        CLStaticHttpHandler webjarsHandler = new CLStaticHttpHandler(loader, "META-INF/resources/webjars/");
        httpServer.getServerConfiguration().addHttpHandler(webjarsHandler, "/webjars/");
        httpServer.start();
    }

}
