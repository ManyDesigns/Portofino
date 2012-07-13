/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.actions.admin.page;

import com.manydesigns.portofino.actions.admin.AdminAction;
import com.manydesigns.portofino.actions.admin.SettingsAction;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pageactions.safemode.SafeModeAction;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.security.RequiresAdministrator;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAdministrator
public abstract class RootConfigurationAction extends PageAdminAction implements AdminAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(SettingsAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    @Override
    @Before
    public Resolution prepare() {
        originalPath = "/";
        File rootDir = application.getPagesDir();
        Page rootPage;
        try {
            rootPage = DispatcherLogic.getPage(rootDir);
        } catch (Exception e) {
            throw new Error("Couldn't load root page", e);
        }
        pageInstance = new PageInstance(null, rootDir, application, rootPage, SafeModeAction.class);
        dispatch = new Dispatch(context.getRequest().getContextPath(), originalPath, pageInstance);
        return null;
    }

    @Buttons({
        @Button(list = "root-permissions", key = "commons.returnToPages", order = 2),
        @Button(list = "root-children", key = "commons.returnToPages", order = 2)
    })
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

}
