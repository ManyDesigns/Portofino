/*
 * Copyright (C) 2016 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.dispatcher;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class NodeWithParameters extends Node {

    protected int minParameters = 0;
    protected int maxParameters = 1;
    
    protected final List<String> parameters = new ArrayList<>();
    
    @Override
    @Path("{pathSegment}")
    public Object consumePathSegment(@PathParam("pathSegment") String pathSegment) {
        if(parameters.size() < minParameters) {
            consumeParameter(pathSegment);
            return this;
        }
        try {
            Object element = super.consumePathSegment(pathSegment);
            parametersAcquired();
            return element;
        } catch (WebApplicationException e) {
            logger.debug("Invalid subresource: " + pathSegment, e);
            if(parameters.size() < maxParameters) {
                consumeParameter(pathSegment);
                if(parameters.size() == maxParameters) {
                    parametersAcquired();
                }
                return this;
            } else {
                throw new WebApplicationException("Too many path parameters", 404);
            }
        }
    }

    public void consumeParameter(String pathSegment) {
        parameters.add(pathSegment);
    }

    protected void parametersAcquired() throws WebApplicationException {
        if(parameters.size() < minParameters) {
            //TODO fail strategy (for mocking and introspection)
            throw new WebApplicationException("Too few path parameters", 404);
        }
    }

    @Override
    public String getPath() {
        StringBuilder path = new StringBuilder(super.getPath());
        for(String param : parameters) {
            path = path.append(param).append("/");
        }
        return path.toString();
    }

    public int getMinParameters() {
        return minParameters;
    }

    public int getMaxParameters() {
        return maxParameters;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
