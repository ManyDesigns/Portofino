package com.manydesigns.portofino.atmosphere.notifications;

import com.manydesigns.portofino.modules.AtmosphereModule;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.PathParam;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.ShiroInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@ManagedService(
        path = Notifications.BASE_PATH + "{topic: [a-zA-Z][a-zA-Z_ \\-0-9]*}",
        interceptors = {
                AtmosphereResourceLifecycleInterceptor.class, SuspendTrackerInterceptor.class, //See https://github.com/Atmosphere/atmosphere/issues/1564
                ShiroInterceptor.class })
public class Notifications {

    private static final Logger logger = LoggerFactory.getLogger(Notifications.class);

    public static final String BASE_PATH = "/m/atmosphere/services/notifications/";

    @PathParam("topic")
    private String topicName;
    private BroadcasterFactory factory;
    private AtmosphereResourceFactory resourceFactory;
    private NotificationService notificationService;

    @Ready
    public void onReady(AtmosphereResource resource) throws Exception {
        factory = resource.getAtmosphereConfig().getBroadcasterFactory();
        resourceFactory = resource.getAtmosphereConfig().resourcesFactory();
        notificationService = (NotificationService)
                resource.getAtmosphereConfig().getServletContext().getAttribute(AtmosphereModule.NOTIFICATION_SERVICE);
        if(!notificationService.subscribe(topicName, resource)) {
            logger.debug("Subscription to topic {} refused (resource: {})", topicName, resource);
            close(resource);
        }
    }

    protected void close(AtmosphereResource r) throws IOException {
        if(r.transport() == AtmosphereResource.TRANSPORT.WEBSOCKET) {
            r.close();
        } else {
            r.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
