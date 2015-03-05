package com.manydesigns.portofino.shiro;

import org.apache.shiro.authc.credential.DefaultPasswordService;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PortofinoPasswordService extends DefaultPasswordService {

    @Override
    protected void checkHashFormatDurability() {
        //Shhh, be quiet.
    }
}
