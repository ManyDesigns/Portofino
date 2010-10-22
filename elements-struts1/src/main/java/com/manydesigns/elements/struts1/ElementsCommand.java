package com.manydesigns.elements.struts1;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.struts.chain.contexts.ServletActionContext;
import com.manydesigns.elements.ElementsThreadLocals;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

/**
 * File created on Oct 22, 2010 at 8:50:17 PM
 * Copyright Paolo Predonzani (paolo.predonzani@gmail.com)
 * All rights reserved
 */
public class ElementsCommand implements Command {
    public boolean execute(Context context) throws Exception {
        ServletActionContext actionContext = (ServletActionContext) context;

        HttpServletRequest request = actionContext.getRequest();
        HttpServletResponse response = actionContext.getResponse();
        ServletContext servletContext = actionContext.getContext();
        
        ElementsThreadLocals.setupDefaultElementsContext();

        ElementsThreadLocals.setHttpServletRequest(request);
        ElementsThreadLocals.setHttpServletResponse(response);
        ElementsThreadLocals.setServletContext(servletContext);

        return CONTINUE_PROCESSING;
    }
}
