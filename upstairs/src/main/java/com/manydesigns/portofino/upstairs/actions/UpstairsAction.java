package com.manydesigns.portofino.upstairs.actions;

import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleRegistry;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.upstairs.ModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
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
//@RequiresAuthentication
//@RequiresAdministrator
public class UpstairsAction extends AbstractPageAction {
    public static final String copyright = "Copyright (C) 2005-2017 ManyDesigns srl";

    public final static Logger logger = LoggerFactory.getLogger(UpstairsAction.class);

    @Autowired
    ModuleRegistry moduleRegistry;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", ModuleRegistry.getPortofinoVersion());
        List<ModuleInfo> modules = new ArrayList<>();
        for(Module module : moduleRegistry.getModules()) {
            ModuleInfo view = new ModuleInfo();
            view.id = module.getId();
            view.name = module.getName();
            view.status = module.getStatus().name();
            view.version = module.getModuleVersion();
            modules.add(view);
        }
        info.put("modules", modules);
        return info;
    }

}
