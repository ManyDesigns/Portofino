package com.manydesigns.portofino.pageactions.crud;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class NotInUseCaseResolution implements Resolution {

    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    private String key;

    public NotInUseCaseResolution(String key) {
        this.key = key;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String msg = ElementsThreadLocals.getText("object.not.found._", key);
        SessionMessages.addWarningMessage(msg);
        new ForwardResolution("/m/pageactions/redirect-to-last-working-page.jsp").execute(request, response);
    }
}
