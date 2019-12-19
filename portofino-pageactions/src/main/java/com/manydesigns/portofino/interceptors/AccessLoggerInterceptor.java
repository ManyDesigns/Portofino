package com.manydesigns.portofino.interceptors;

import com.manydesigns.portofino.pageactions.log.LogAccesses;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@Intercepts(LifecycleStage.EventHandling)
public class AccessLoggerInterceptor implements Interceptor {

    public static final Logger logger = LoggerFactory.getLogger(AccessLoggerInterceptor.class);

    @Override
    public Resolution intercept(ExecutionContext context) throws Exception {
        Object bean = context.getActionBean();
        Method handler = context.getHandler();
        if(isToBeLogged(bean, handler)) {
            logger.info("ActionBean, method " + handler + ", event " + context.getActionBeanContext().getEventName());
        }
        return context.proceed();
    }

    public static boolean isToBeLogged(Object resource, Method handler) {
        if (resource != null) {
            Boolean log = null;
            Class<?> resourceClass = resource.getClass();

            LogAccesses annotation;
            if(handler != null) {
                annotation = handler.getAnnotation(LogAccesses.class);
                if(annotation != null) {
                    log = annotation.value();
                }
            }
            if(log == null) {
                annotation = resourceClass.getAnnotation(LogAccesses.class);
                log = (annotation != null && annotation.value());
            }
            return log;
        }
        return false;
    }
}
