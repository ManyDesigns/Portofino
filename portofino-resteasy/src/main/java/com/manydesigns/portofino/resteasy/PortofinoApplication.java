package com.manydesigns.portofino.resteasy;

import com.manydesigns.portofino.pageactions.rest.APIRoot;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath(APIRoot.PATH_PREFIX)
public class PortofinoApplication extends Application {

}
