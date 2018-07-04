package com.manydesigns.portofino.dispatcher.visitor;

import com.manydesigns.portofino.dispatcher.Node;
import com.manydesigns.portofino.dispatcher.NodeWithParameters;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.vfs2.FileType.FOLDER;

/**
 * Created by alessio on 17/05/16.
 */
public class DepthFirstVisitor {
    
    protected final List<NodeVisitor> visitors = new ArrayList<>();
    protected static final Logger logger = LoggerFactory.getLogger(DepthFirstVisitor.class);
    
    public DepthFirstVisitor(NodeVisitor... visitors) {
        this.visitors.addAll(Arrays.asList(visitors));
    }

    public List<NodeVisitor> getVisitors() {
        return visitors;
    }

    public void visit(Node node) throws Exception {
        for(NodeVisitor visitor : visitors) {
            visitor.visit(node);
        }
        for(String subResourceName : node.getSubResources()) {
            try {
                if(node instanceof NodeWithParameters) {
                    for(int i = 0; i < ((NodeWithParameters) node).getMinParameters(); i++) {
                        ((NodeWithParameters) node).consumeParameter("_" + i);
                    }
                }
                Object element = node.getSubResource(subResourceName);
                if (element instanceof Node) {
                    visit((Node) element);
                }
            } catch (Exception e) {
                logger.error("Could not visit node " + node.getLocation().getURL(), e);
            }
        }
    }
    
}
