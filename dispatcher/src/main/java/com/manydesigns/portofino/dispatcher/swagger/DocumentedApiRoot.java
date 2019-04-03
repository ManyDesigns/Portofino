package com.manydesigns.portofino.dispatcher.swagger;

import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.dispatcher.visitor.DepthFirstVisitor;
import com.manydesigns.portofino.dispatcher.visitor.ResourceVisitor;
import io.swagger.annotations.Api;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ResourceContext;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alessio on 28/07/16.
 */
@Api
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
    public void beforeScan(Reader reader, Swagger swagger) {}

    @Override
    public void afterScan(Reader reader, Swagger swagger) {
        final SubResourceReader subResourceReader = getSubResourceReader(reader);
        try {
            //TODO actions should be put in a special "inspection mode" to avoid checks (e.g. not-in-use-case),
            //hitting the DB or services, etc.
            Resource root = rootFactory.createRoot();
            root.setResourceContext(getResourceContext());
            new DepthFirstVisitor((ResourceVisitor) node -> {
                try {
                    subResourceReader.readSubResource(node);
                } catch (Exception e) {
                    logger.error("Could not read node at " + node.getLocation(), e);
                }
            }).visit(root);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected SubResourceReader getSubResourceReader(Reader reader) {
        return new SubResourceReader(reader);
    }

    protected ResourceContext getResourceContext() {
        return new DummyResourceContext();
    }

    protected static class SubResourceReader extends Reader {

        public SubResourceReader(Reader reader) {
            super(reader.getSwagger(), reader.getConfig());
        }

        public Swagger readSubResource(Resource resource) {
            ArrayList<Parameter> parameters = new ArrayList<>();
            String path = calculateResourcePath(resource, parameters);

            return read(resource.getClass(), path, null, true, new String[0], new String[0],
                    new HashMap<>(), parameters);
        }

        public String calculateResourcePath(Resource resource, ArrayList<Parameter> parameters) {
            String path;
            if(resource.getParent() != null) {
                path = calculateResourcePath(resource.getParent(), parameters);
                path += "/" + resource.getSegment();
            } else {
                path = "";
            }
            if(resource instanceof WithParameters) {
                WithParameters wp = (WithParameters) resource;
                for(int i = 0; i < wp.getParameters().size(); i++) {
                    boolean required = i < wp.getMinParameters();
                    String name = wp.getParameterName(i);
                    path += "{" + name + "}";
                    PathParameter parameter = new PathParameter();
                    parameter.setName(name);
                    parameter.setRequired(required);
                    parameters.add(parameter);
                }
            }
            return path;
        }
    }

    public static class DummyResourceContext implements ResourceContext {
        @Override
        public <T> T getResource(Class<T> resourceClass) {
            try {
                return initResource(resourceClass.newInstance());
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
