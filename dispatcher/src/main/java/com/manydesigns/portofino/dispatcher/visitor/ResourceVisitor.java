package com.manydesigns.portofino.dispatcher.visitor;

import com.manydesigns.portofino.dispatcher.AbstractResource;
import com.manydesigns.portofino.dispatcher.Resource;

/**
 * Created by alessio on 17/05/16.
 */
public interface ResourceVisitor {
    
    void visit(Resource resource) throws Exception;
    
}
