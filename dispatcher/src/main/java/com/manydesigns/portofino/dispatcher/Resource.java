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

import org.apache.commons.vfs2.FileObject;

import javax.ws.rs.PathParam;
import javax.ws.rs.container.ResourceContext;
import java.util.Collection;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface Resource {
    
    FileObject getLocation();

    void setLocation(FileObject location);

    ResourceResolver getResourceResolver();

    Resource getParent();

    void setParent(Resource resource);

    String getPath();

    void setResourceContext(ResourceContext resourceContext);
    
    Collection<String> getSubResources();

    Object getSubResource(String name) throws Exception;
    
    String getSegment();
    
    void setSegment(String segment);

    Object consumePathSegment(String pathSegment);

    void init();

}
