package com.manydesigns.portofino.buttons;

import net.sourceforge.stripes.action.Resolution;

import java.lang.reflect.Method;

/**
 * Created by alessio on 22/12/16.
 */
public interface Guarded {
    
    Resolution guardsFailed(Method handler);
    
}
