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

import com.manydesigns.portofino.dispatcher.ResourceResolver;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class CachingResourceResolver implements ResourceResolver {
    
    private final ConcurrentMap<String, Cached> cache = new ConcurrentHashMap<>();
    protected static final Logger logger = LoggerFactory.getLogger(CachingResourceResolver.class);
    protected ResourceResolver delegate;
    
    protected CachingResourceResolver() {}
    
    public CachingResourceResolver(ResourceResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean supports(Class<?> type) {
        return delegate.supports(type);
    }
    
    @Override
    public boolean supports(String extension) {
        return delegate.supports(extension);
    }

    @Override
    public <T> T resolve(FileObject location, Class<T> type) throws Exception {
        if(location == null) {
            return null;
        }
        location = resolve(location);
        if(location == null) {
            return null;
        }
        String key = location.getName().getURI() + " " + type.getName();
        Cached<T> cached = cache.get(key);
        if(cached == null) {
            return cache(key, location, type).value;
        } else {
            long lastModifiedTime = getLastModifiedTime(location);
            if(lastModifiedTime > cached.timestamp) {
                logger.debug("Timestamp for {} is {}, was cached with last-modified time {}", location, lastModifiedTime, cached.timestamp);
                return cache(key, location, type).value;
            } else {
                return type.cast(cached.value);
            }
        }
    }

    @Override
    public FileObject resolve(FileObject location) throws FileSystemException {
        //TODO cache this too?
        return delegate.resolve(location);
    }

    @Override
    public FileObject resolve(FileObject location, String name) throws FileSystemException {
        //TODO cache this too?
        return delegate.resolve(location, name);
    }

    @Override
    public <T> T resolve(FileObject location, String name, Class<T> type) throws Exception {
        return resolve(resolve(location, name), type);
    }

    protected long getLastModifiedTime(FileObject location) throws FileSystemException {
        return location.getContent().getLastModifiedTime();
    }

    protected <T> Cached<T> cache(String key, FileObject location, Class<T> type) throws Exception {
        Cached<T> value = resolveForCache(location, type);
        cache.put(key, value);
        return value;
    }

    protected <T> Cached<T> resolveForCache(FileObject location, Class<T> type) throws Exception {
        return new Cached<T>(doResolve(location, type), location.getContent().getLastModifiedTime());
    }

    protected <T> T doResolve(FileObject location, Class<T> type) throws Exception {
        return delegate.resolve(location, type);
    }

    public void clearCache(long maxAge) {
        long now = System.currentTimeMillis();
        for(Map.Entry<String, Cached> entry : cache.entrySet()) {
            if(now - entry.getValue().timestamp > maxAge) {
                removeCacheEntry(entry);
            }
        }
    }

    protected Cached removeCacheEntry(Map.Entry<String, Cached> entry) {
        return cache.remove(entry.getKey());
    }

    public static class Cached<T> {
        public final T value;
        public final long timestamp;

        public Cached(T value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
