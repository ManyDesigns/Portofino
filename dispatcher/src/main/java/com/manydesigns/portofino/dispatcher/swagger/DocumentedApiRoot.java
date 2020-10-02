package com.manydesigns.portofino.dispatcher.swagger;

import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.dispatcher.RootFactory;
import com.manydesigns.portofino.dispatcher.WithParameters;
import com.manydesigns.portofino.dispatcher.visitor.DepthFirstVisitor;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.jaxrs2.ReaderListener;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ResourceContext;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by alessio on 28/07/16.
 */
public abstract class DocumentedApiRoot implements ReaderListener {
    
    protected static final Logger logger = LoggerFactory.getLogger(DocumentedApiRoot.class);
    protected static RootFactory rootFactory;

    public static void setRootFactory(RootFactory rootFactory) {
        DocumentedApiRoot.rootFactory = rootFactory;
    }

    public static RootFactory getRootFactory() {
        return rootFactory;
    }
    
    @Override
    public void beforeScan(OpenApiReader reader, OpenAPI openAPI) {}

    @Override
    public void afterScan(OpenApiReader reader, OpenAPI openAPI) {
        final SubResourceReader subResourceReader = getSubResourceReader(openAPI);
        try {
            //TODO actions should be put in a special "inspection mode" to avoid checks (e.g. not-in-use-case),
            //hitting the DB or services, etc.
            Resource root = rootFactory.createRoot();
            root.setResourceContext(getResourceContext());
            initRoot(root);
            new DepthFirstVisitor(node -> {
                try {
                    OpenAPI subApi = subResourceReader.readSubResource(node);
                    openAPI.getPaths().putAll(subApi.getPaths());
                    //TODO merge components too
                } catch (Exception e) {
                    logger.error("Could not read node at " + node.getLocation(), e);
                }
            }).visit(root);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void initRoot(Resource root) {
        root.init();
    }

    protected SubResourceReader getSubResourceReader(OpenAPI openAPI) {
        return new SubResourceReader(openAPI);
    }

    protected ResourceContext getResourceContext() {
        return new DummyResourceContext();
    }

    protected static class SubResourceReader extends Reader {

        public SubResourceReader(OpenAPI openAPI) {
            super(openAPI);
        }

        public OpenAPI readSubResource(Resource resource) {
            ArrayList<Parameter> parameters = new ArrayList<>();
            String path = calculateResourcePath(resource, parameters);

            return read(resource.getClass(), path, null, true, null, null,
                    new HashSet<>(), parameters, new HashSet<>());
        }

        public String calculateResourcePath(Resource resource, ArrayList<Parameter> parameters) {
            StringBuilder path;
            if(resource.getParent() != null) {
                path = new StringBuilder(calculateResourcePath(resource.getParent(), parameters));
                path.append("/").append(resource.getSegment());
            } else {
                path = new StringBuilder();
            }
            if(resource instanceof WithParameters) {
                WithParameters wp = (WithParameters) resource;
                for(int i = 0; i < wp.getParameters().size(); i++) {
                    String name = wp.getParameterName(i);
                    path.append("/{").append(name).append("}");
                    PathParameter parameter = new PathParameter();
                    parameter.setName(name);
                    parameter.setRequired(true);
                    parameters.add(parameter);
                }
            }
            return path.toString();
        }
    }

    public static class DummyResourceContext implements ResourceContext {
        @Override
        public <T> T getResource(Class<T> resourceClass) {
            try {
                return initResource(resourceClass.getConstructor().newInstance());
            } catch (Exception e) {
                logger.warn("Could not create resource for Swagger", e);
                return null;
            }
        }

        @Override
        public <T> T initResource(T resource) {
            if(resource instanceof Resource) {
                ((Resource) resource).setResourceContext(this);
            }
            return resource;
        }
    }
    
}
