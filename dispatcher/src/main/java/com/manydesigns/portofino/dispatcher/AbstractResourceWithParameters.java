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
import java.util.Map;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class AbstractResourceWithParameters extends AbstractResource implements WithParameters {
    public static final String COPYRIGHT = "Copyright (C) 2005-2020 ManyDesigns srl";

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
        Object element;
        try {
            element = super.consumePathSegment(pathSegment);
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
        parametersAcquired();
        return element;
    }

    public void consumeParameter(String pathSegment) {
        parameters.add(pathSegment);
    }

    /**
     * Lifecycle method invoked when there are no more path parameters to process.
     * @since 5.0.0
     */
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
            path.append(param).append("/");
        }
        return path.toString();
    }

    @Override
    public int getMinParameters() {
        return minParameters;
    }

    @Override
    public int getMaxParameters() {
        return maxParameters;
    }

    @Override
    public List<String> getParameters() {
        return parameters;
    }

    @Override
    public String getParameterName(int index) {
        if(index < getMinParameters()) {
            return "requiredPathParameter_" + index;
        } else {
            return "optionalPathParameter_" + index;
        }
    }

    @Override
    public Map<String, Object> describe() {
        Map<String, Object> description = super.describe();
        description.put("minParameters", minParameters);
        description.put("maxParameters", maxParameters);
        description.put("parameters", parameters);
        return description;
    }
}
