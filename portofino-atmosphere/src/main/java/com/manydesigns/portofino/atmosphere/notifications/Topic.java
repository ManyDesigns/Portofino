package com.manydesigns.portofino.atmosphere.notifications;

import org.atmosphere.cpr.AtmosphereResource;

import java.util.regex.Pattern;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Topic {

    public final Pattern pattern;

    public Topic(Pattern pattern) {
        this.pattern = pattern;
    }

    public Topic(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public boolean canSubscribe(AtmosphereResource resource) {
        return true;
    }
}
