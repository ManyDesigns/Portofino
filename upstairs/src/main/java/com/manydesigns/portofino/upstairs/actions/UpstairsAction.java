package com.manydesigns.portofino.upstairs.actions;

import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.upstairs.ModuleInfo;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class UpstairsAction extends AbstractPageAction {
    public static final String copyright = "Copyright (C) 2005-2017 ManyDesigns srl";

    public final static Logger logger = LoggerFactory.getLogger(UpstairsAction.class);

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CodeBase codeBase;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", PortofinoProperties.getPortofinoVersion());
        List<ModuleInfo> modules = new ArrayList<>();
        for(Module module : applicationContext.getBeansOfType(Module.class).values()) {
            ModuleInfo view = new ModuleInfo();
            view.moduleClass = module.getClass().getName();
            view.name = module.getName();
            view.status = module.getStatus().name();
            view.version = module.getModuleVersion();
            modules.add(view);
        }
        info.put("modules", modules);
        return info;
    }

    @POST
    @Path(":restart")
    public void restartApplication() throws Exception {
        codeBase.clear();
        OgnlUtils.clearCache();
        if(applicationContext instanceof ConfigurableApplicationContext) {
            //Spring enhances @Configuration classes. To do so it loads them by name from its classloader.
            //Thus, replacing the classloader with a fresh one has also the side-effect of making Spring reload the user
            //SpringConfiguration class, provided it already existed and was annotated with @Configuration.
            //Note that Spring won't pick up new @Bean methods. It will also barf badly on removed @Bean methods,
            //effectively crashing the application. Changing the return value and even the return type is fine.
            ((DefaultResourceLoader) applicationContext).setClassLoader(codeBase.asClassLoader());
            ((ConfigurableApplicationContext) applicationContext).refresh();
        }
    }

}
