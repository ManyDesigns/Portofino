package com.manydesigns.portofino.stripes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.WebApplicationException;

/**
 * Silently ignores WebApplicationExceptions (since they're handled by Jax-Rs)
 *
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class JaxRsExceptionHandler {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(JaxRsExceptionHandler.class);

    public void handle(WebApplicationException ex, HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Ignoring WebApplicationException", ex);
    }

}
