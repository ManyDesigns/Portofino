package com.manydesigns.portofino.dispatcher.visitor;

import com.manydesigns.portofino.dispatcher.Node;

/**
 * Created by alessio on 17/05/16.
 */
public interface NodeVisitor {
    
    void visit(Node node) throws Exception;
    
}
