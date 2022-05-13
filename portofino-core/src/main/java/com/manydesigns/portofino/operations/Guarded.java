package com.manydesigns.portofino.operations;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.Method;

/**
 * Created by alessio on 22/12/16.
 */
public interface Guarded {
    
    Response guardsFailed(Method handler);
    
}
