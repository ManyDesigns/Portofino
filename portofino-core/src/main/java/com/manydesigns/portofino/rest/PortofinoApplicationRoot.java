package com.manydesigns.portofino.rest;

import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.dispatcher.Root;
import com.manydesigns.portofino.dispatcher.security.ResourcePermissions;
import com.manydesigns.portofino.dispatcher.web.ApplicationRoot;
import io.swagger.converter.ModelConverters;
import io.swagger.jackson.ModelResolver;
import io.swagger.jackson.TypeNameResolver;
import io.swagger.jaxrs.Reader;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import org.apache.commons.vfs2.FileObject;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;
import java.lang.reflect.*;
import java.net.URLStreamHandlerFactory;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Set;

@Path("/")
public class PortofinoApplicationRoot extends ApplicationRoot {

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
    public void beforeScan(Reader reader, Swagger swagger) {
        super.beforeScan(reader, swagger);
        ModelConverters.getInstance().addConverter(new PortofinoModelResolver(Json.mapper()));
    }

    @Override
    public void afterScan(Reader reader, Swagger swagger) {
        super.afterScan(reader, swagger);
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