/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.actions.admin.page;

import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.actions.admin.AdminAction;
import com.manydesigns.portofino.actions.admin.SettingsAction;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pageactions.safemode.SafeModeAction;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.security.RequiresAdministrator;
import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.LifecycleStage;
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
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(SettingsAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    @Override
    @After(stages = LifecycleStage.BindingAndValidation)
    public void prepare() {
        originalPath = "/";
        application = (Application) context.getRequest().getAttribute(RequestAttributes.APPLICATION);
        File rootDir = application.getPagesDir();
        Page rootPage;
        try {
            rootPage = DispatcherLogic.getPage(rootDir);
        } catch (Exception e) {
            throw new Error("Couldn't load root page", e);
        }
        PageInstance rootPageInstance = new PageInstance(null, rootDir, application, rootPage);
        rootPageInstance.setActionClass(SafeModeAction.class); //To avoid errors when accessing dispatch.actionClass
        dispatch = new Dispatch(context.getRequest().getContextPath(), originalPath, rootPageInstance);
        context.getRequest().setAttribute(RequestAttributes.DISPATCH, dispatch);
    }

    @Buttons({
        @Button(list = "root-permissions", key = "commons.returnToPages", order = 2),
        @Button(list = "root-children", key = "commons.returnToPages", order = 2)
    })
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

}
