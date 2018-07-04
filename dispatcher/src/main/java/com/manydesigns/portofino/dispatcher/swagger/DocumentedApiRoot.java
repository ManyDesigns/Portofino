package com.manydesigns.portofino.dispatcher.swagger;

import com.manydesigns.portofino.dispatcher.Node;
import com.manydesigns.portofino.dispatcher.NodeWithParameters;
import com.manydesigns.portofino.dispatcher.Root;
import com.manydesigns.portofino.dispatcher.visitor.DepthFirstVisitor;
import com.manydesigns.portofino.dispatcher.visitor.NodeVisitor;
import io.swagger.annotations.Api;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.properties.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alessio on 28/07/16.
 */
@Api
public abstract class DocumentedApiRoot implements ReaderListener {
    
    protected static final Logger logger = LoggerFactory.getLogger(DocumentedApiRoot.class);
    protected static Root root;

    public static void setRoot(Root root) {
        DocumentedApiRoot.root = root;
    }

    public static Root getRoot() {
        return root;
    }
    
    @Override
    public void beforeScan(Reader reader, Swagger swagger) {

    }

    @Override
    public void afterScan(Reader reader, Swagger swagger) {
        final SubResourceReader subResourceReader = new SubResourceReader(reader);
        try {
            new DepthFirstVisitor(new NodeVisitor() {
                @Override
                public void visit(Node node) throws Exception {
                    try {
                        subResourceReader.readSubResource(node);
                    } catch (Exception e) {
                        logger.error("Could not read node at " + node.getLocation(), e);
                    }
                }
            }).visit(root);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    static class SubResourceReader extends Reader {

        public SubResourceReader(Reader reader) {
            super(reader.getSwagger(), reader.getConfig());
        }

        public Swagger readSubResource(Node node) {
            ArrayList<Parameter> parameters = new ArrayList<>();
            String path = node.getPath();
            if(node instanceof NodeWithParameters) {
                NodeWithParameters withParameters = (NodeWithParameters) node;
                int minParameters = withParameters.getMinParameters();
                int maxParameters = withParameters.getMaxParameters();
                for(int i = 0; i < maxParameters; i++) {
                    PathParameter parameter = new PathParameter();
                    String paramName = "param" + i;
                    parameter.setName(paramName);
                    path += "/{" + paramName + "}";
                    parameter.setProperty(new StringProperty());
                    parameter.setRequired(i < minParameters);
                    parameters.add(parameter);
                }
            }
            return read(node.getClass(), path, null, true, new String[0], new String[0], new HashMap<String, Tag>(), parameters);
        }
    }
    
}
