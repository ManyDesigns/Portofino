package com.manydesigns.portofino.dispatcher.security;

import com.manydesigns.portofino.dispatcher.Resource;

/**
 * Created by alessio on 7/20/16.
 */
public interface SecureResource extends Resource {

    ResourcePermissions getPermissions();

}
