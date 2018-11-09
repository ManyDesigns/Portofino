package com.manydesigns.portofino.dispatcher.visitor;

import com.manydesigns.portofino.dispatcher.AbstractResource;
import com.manydesigns.portofino.dispatcher.AbstractResourceWithParameters;
import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.dispatcher.WithParameters;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alessio on 17/05/16.
 */
public class DepthFirstVisitor {
    
    protected final List<ResourceVisitor> visitors = new ArrayList<>();
    protected static final Logger logger = LoggerFactory.getLogger(DepthFirstVisitor.class);
    
    public DepthFirstVisitor(ResourceVisitor... visitors) {
        this.visitors.addAll(Arrays.asList(visitors));
    }

    public List<ResourceVisitor> getVisitors() {
        return visitors;
    }

    public void visit(Resource resource) throws Exception {
        if(resource instanceof AbstractResourceWithParameters) {
            for(int i = 0; i < ((WithParameters) resource).getMinParameters(); i++) {
                ((WithParameters) resource).consumeParameter("{requiredPathParameter}");
            }
        }
        visitResource(resource);
        if(resource instanceof AbstractResourceWithParameters && ((WithParameters) resource).getMinParameters() == 0) {
            for(int i = 0; i < ((WithParameters) resource).getMaxParameters(); i++) {
                ((WithParameters) resource).consumeParameter("{optionalPathParameter}");
            }
            visitResource(resource);
        }
    }

    protected void visitResource(Resource resource) throws Exception {
        for(ResourceVisitor visitor : visitors) {
            visitor.visit(resource);
        }
        visitSubResources(resource);
    }

    protected void visitSubResources(Resource resource) throws FileSystemException {
        for(String subResourceName : resource.getSubResources()) {
            try {
                Object element = resource.getSubResource(subResourceName);
                if (element instanceof AbstractResource) {
                    visit((AbstractResource) element);
                }
            } catch (Exception e) {
                logger.error("Could not visit resource " + resource.getLocation().getURL(), e);
            }
        }
    }

}
