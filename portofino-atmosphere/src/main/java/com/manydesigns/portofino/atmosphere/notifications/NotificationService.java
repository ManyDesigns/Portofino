package com.manydesigns.portofino.atmosphere.notifications;

import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class NotificationService {

    private AtmosphereFramework framework;

    public NotificationService(AtmosphereFramework framework) {
        this.framework = framework;
    }

    public final List<Topic> topics = new CopyOnWriteArrayList<Topic>();

    public boolean subscribe(String topicName, AtmosphereResource resource) {
        Topic topic = getTopic(topicName);
        return topic == null || topic.canSubscribe(resource);
    }

    public Topic getTopic(String topicName) {
        for(Topic topic : topics) {
            if(topic.pattern.matcher(topicName).matches()) {
                return topic;
            }
        }
        return null;
    }

    public boolean sendNotification(String topicName, String message) throws IOException {
        BroadcasterFactory broadcasterFactory = framework.getBroadcasterFactory();
        Broadcaster b =  broadcasterFactory.lookup(Notifications.BASE_PATH + topicName);
        if(b != null) {
            b.broadcast(message);
            return true;
        } else {
            return false;
        }
    }

}
