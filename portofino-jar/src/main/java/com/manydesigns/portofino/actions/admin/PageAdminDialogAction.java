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

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.portofino.actions.AbstractActionBean;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.actions.forms.MovePage;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.Dispatcher;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.PageLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.system.model.users.annotations.RequiresAdministrator;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding("/actions/admin/page/dialog")
public class PageAdminDialogAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    @Inject(RequestAttributes.MODEL)
    public Model model;

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    protected String pagePath;
    protected PageInstance pageInstance;
    protected Form moveForm;
    protected Form copyForm;

    @Before
    public void prepare() {
        Dispatcher dispatcher = new Dispatcher(application);
        Dispatch dispatch = dispatcher.createDispatch(context.getRequest().getContextPath(), pagePath);
        pageInstance = dispatch.getLastPageInstance();
    }

    @RequiresAdministrator
    public Resolution confirmDelete() {
        return new ForwardResolution("/layouts/admin/deletePageDialog.jsp");
    }

    @RequiresAdministrator
    public Resolution chooseNewLocation() {
        SelectionProvider pagesSelectionProvider =
                PageLogic.createPagesSelectionProvider
                        (application, application.getPagesDir(), true, true, pageInstance.getDirectory());
        moveForm = new FormBuilder(MovePage.class)
                .configReflectiveFields()
                .configSelectionProvider(pagesSelectionProvider, "destinationPagePath")
                .build();
        return new ForwardResolution("/layouts/admin/movePageDialog.jsp");
    }

    @RequiresAdministrator
    public Resolution copyPageDialog() {
        return null; //TODO ripristinare

        /*
        SelectionProvider pagesSelectionProvider =
                PageLogic.createPagesSelectionProvider(model.getRootPage(), true, true, page);
        copyForm = new FormBuilder(CopyPage.class)
                .configReflectiveFields()
                .configSelectionProvider(pagesSelectionProvider, "destinationPagePath")
                .build();
        return new ForwardResolution("/layouts/admin/copyPageDialog.jsp");*/
    }

    public String getPagePath() {
        return pagePath;
    }

    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    public PageInstance getPageInstance() {
        return pageInstance;
    }

    public Form getMoveForm() {
        return moveForm;
    }

    public Form getCopyForm() {
        return copyForm;
    }
}
