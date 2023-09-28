package com.manydesigns.portofino.rest;

import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.dispatcher.Root;
import com.manydesigns.portofino.dispatcher.security.ResourcePermissions;
import com.manydesigns.portofino.dispatcher.swagger.DocumentedApiRoot;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.jackson.TypeNameResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import org.apache.commons.vfs2.FileObject;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.lang.reflect.*;
import java.net.URLStreamHandlerFactory;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Path("/")
public class PortofinoApplicationRoot extends DocumentedApiRoot {

    @Context
    protected ServletConfig config;
    @Context
    protected Application application;
    @Context
    protected ResourceContext resourceContext;

    @PostConstruct
    public void init() {
        try {
            new JaxrsOpenApiContextBuilder()
                    .servletConfig(config)
                    .application(application)
                    .resourceClasses(Collections.singleton(getClass().getName()))
                    .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ResourceContext getResourceContext() {
        ServletContext servletContext = ElementsThreadLocals.getServletContext();
        final WebApplicationContext applicationContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        return new DummyResourceContext() {
            @Override
            public <T> T initResource(T resource) {
                super.initResource(resource);
                applicationContext.getAutowireCapableBeanFactory().autowireBean(resource);
                return resource;
            }
        };
    }

    @Override
    protected void initRoot(Resource root) {
        ((PortofinoRoot) root).servletContext = ElementsThreadLocals.getServletContext();
        ((PortofinoRoot) root).request = ElementsThreadLocals.getHttpServletRequest();
        ((PortofinoRoot) root).response = ElementsThreadLocals.getHttpServletResponse();
        root.init();
    }

    @Override
    public void beforeScan(OpenApiReader reader, OpenAPI openAPI) {
        super.beforeScan(reader, openAPI);
        ModelConverters.getInstance().addConverter(new PortofinoModelResolver(Json.mapper()));
    }

    @Path("")
    public Object start() throws Exception {
        Resource root = rootFactory.createRoot();
        resourceContext.initResource(root);
        return root.init();
    }
}

class PortofinoModelResolver extends ModelResolver {

    //Note: it doesn't consider inheritance
    protected final Set<Class<?>> classesToIgnore = new HashSet<>();

    {
        classesToIgnore.add(AnnotatedType.class);
        classesToIgnore.add(Certificate.class);
        classesToIgnore.add(Constructor.class);
        classesToIgnore.add(Executable.class);
        classesToIgnore.add(FileObject.class);
        classesToIgnore.add(GenericDeclaration.class);
        classesToIgnore.add(PublicKey.class);
        classesToIgnore.add(Resource.class);
        classesToIgnore.add(ResourcePermissions.class);
        classesToIgnore.add(ResourceResolver.class);
        classesToIgnore.add(Root.class);
        classesToIgnore.add(Type.class);
        classesToIgnore.add(URLStreamHandlerFactory.class);
    }

    public PortofinoModelResolver(ObjectMapper mapper) {
        super(mapper);
    }

    public PortofinoModelResolver(ObjectMapper mapper, TypeNameResolver typeNameResolver) {
        super(mapper, typeNameResolver);
    }

    @Override
    protected boolean shouldIgnoreClass(Type type) {
        if(!super.shouldIgnoreClass(type)) {
            if(type instanceof Class) {
                return classesToIgnore.contains(type);
            } else if(type instanceof ResolvedType) {
                return classesToIgnore.contains(((ResolvedType) type).getRawClass());
            }
        }
        return true;
    }
}
