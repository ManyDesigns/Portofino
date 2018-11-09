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

package com.manydesigns.portofino.dispatcher.resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.vfs2.FileObject;

import java.io.InputStream;

/**
 * Created by alessio on 13/07/16.
 */
public class JacksonResourceResolver extends AbstractResourceResolver {
    
    protected final ObjectMapper objectMapper;

    public JacksonResourceResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JacksonResourceResolver() {
        this(new ObjectMapper());
    }

    @Override
    public boolean supports(Class<?> type) {
        return 
                type != Class.class && //To avoid conflicting with resource resolvers for actions
                objectMapper.canDeserialize(TypeFactory.defaultInstance().constructType(type));
    }

    @Override
    public boolean supports(String extension) {
        return "json".equals(extension);
    }

    @Override
    public <T> T resolve(FileObject location, Class<T> type) throws Exception {
        FileObject resource = resolve(location);
        if(resource == null) {
            return null;
        }
        try(InputStream inputStream = resource.getContent().getInputStream()) {
            return objectMapper.readValue(inputStream, type);
        }
    }

}
