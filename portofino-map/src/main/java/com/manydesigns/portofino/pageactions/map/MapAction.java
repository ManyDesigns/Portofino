/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.pageactions.map;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.map.configuration.MapConfiguration;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emanuele Poggi    - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ConfigurationClass(MapConfiguration.class)
@PageActionName("Map")
@ScriptTemplate("script_template.groovy")
public class MapAction extends AbstractPageAction {
    public static final String copyright = "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    //**************************************************************************
    // Variables
    //**************************************************************************

    protected MapView mapView;
    protected final Map map = new Map();

    //**************************************************************************
    // Support objects
    //**************************************************************************

    protected Form configurationForm;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(MapAction.class);

    //**************************************************************************
    // Setup and configuration
    //**************************************************************************

    public Resolution preparePage() {
        if(pageInstance.getConfiguration() == null) {
            pageInstance.setConfiguration(new MapConfiguration());
        }
        return null;
    }

    @Button(list = "pageHeaderButtons", titleKey = "configure", order = 1, icon = Button.ICON_WRENCH)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/m/map/configure.jsp");
    }

    @Button(list = "configuration", key = "update.configuration", type = Button.TYPE_PRIMARY)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution updateConfiguration() {
        prepareConfigurationForms();
        readPageConfigurationFromRequest();
        configurationForm.readFromRequest(context.getRequest());
        boolean valid = validatePageConfiguration();
        valid = valid && configurationForm.validate();
        if(valid) {
            updatePageConfiguration();
            configurationForm.writeToObject(pageInstance.getConfiguration());
            saveConfiguration(pageInstance.getConfiguration());
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("configuration.updated.successfully"));
            return cancel();
        } else {
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("the.configuration.could.not.be.saved"));
            return new ForwardResolution("/m/map/configure.jsp");
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        configurationForm = new FormBuilder(MapConfiguration.class).build();
        configurationForm.readFromObject(pageInstance.getConfiguration());
    }

    //**************************************************************************
    // Handlers
    //**************************************************************************

    @DefaultHandler
    @RequiresPermissions(level = AccessLevel.VIEW)
    public Resolution execute() {
            return mapView();
    }

    public Resolution mapView() {
        return new ForwardResolution("/m/map/map.jsp");
    }

    //--------------------------------------------------------------------------
    // Data provider
    //--------------------------------------------------------------------------

    public void loadObjects() {}

    public MapView getMapView() {
        return mapView;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public Map getMap() {
        return map;
    }

    public List<Marker> getMarkers() {
        return map.getMarkers();
    }

    public Form getConfigurationForm() {
        return configurationForm;
    }

    public MapConfiguration getConfiguration() {
        return (MapConfiguration) pageInstance.getConfiguration();
    }

    public Resolution getJsonMarkers(){
        JSONObject result =  new JSONObject();
        JSONArray markers = new JSONArray();

        loadObjects();
        result.put("geolocation",getConfiguration().getGeolocation());
        result.put("lat",getConfiguration().getLatitude());
        result.put("lon",getConfiguration().getLongitude());
        result.put("zoom",getConfiguration().getZoom());

        for( Marker marker : map.getMarkers() ){
            JSONObject m = new JSONObject();
            m.put("title",marker.getTitle());
            m.put("description",marker.getDescription());
            m.put("lat",marker.getPosition().getLat());
            m.put("lon",marker.getPosition().getLon());
            m.put("url",marker.getReadUrl());
            markers.put(m);
        }
        result.put("markers",markers);

        return new StreamingResolution("application/json", result.toString());

    }
}
