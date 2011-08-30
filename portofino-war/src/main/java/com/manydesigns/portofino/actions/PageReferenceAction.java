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

package com.manydesigns.portofino.actions;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.Dispatcher;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.dispatcher.PageReferenceInstance;
import com.manydesigns.portofino.logic.PageLogic;
import com.manydesigns.portofino.model.pages.PageReference;
import net.sourceforge.stripes.action.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding("/ref.action")
public class PageReferenceAction extends PortletAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected PageReference pageReference;

    protected Form form;

    protected String includeActionPath;

    public static final Logger logger =
            LoggerFactory.getLogger(PageReferenceAction.class);

    @Inject(RequestAttributes.DISPATCH)
    public Dispatch dispatch;

    @Before
    @Override
    public void prepare() {
        super.prepare();
        pageReference = (PageReference) getPageInstance().getPage();
    }

    @Override
    protected void dereferencePageInstance() {
        //Do nothing. I need the raw PageReferenceInstance.
    }

    @DefaultHandler
    public Resolution execute() {
        if(pageReference.getToPage() == null) {
            if(isEmbedded()) {
                return new ForwardResolution(PAGE_PORTLET_NOT_CONFIGURED);
            } else {
                return forwardToPortletPage(PAGE_PORTLET_NOT_CONFIGURED);
            }
        } else {
            //Never embed in this case - the referenced action will take
            //care of it.
            includeActionPath = Dispatcher.getRewrittenPath(pageReference.getToPage());
            return new ForwardResolution(includeActionPath);
        }
    }

    public Resolution configure() {
        setupPageConfiguration();
        return new ForwardResolution("/layouts/page/configure.jsp");
    }

    public Resolution updateConfiguration() {
        synchronized (application) {
            setupPageConfiguration();
            readPageConfigurationFromRequest();
            form.readFromRequest(context.getRequest());
            boolean valid = validatePageConfiguration();
            valid = form.validate() && valid;
            if(valid) {
                updatePageConfiguration();
                form.writeToObject(pageReference);
                saveModel();
                SessionMessages.addInfoMessage("Configuration updated successfully");
            }
            return cancel();
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();

        SelectionProvider pagesSelectionProvider =
                PageLogic.createPagesSelectionProvider(model.getRootPage(), true, true, pageReference);

        form = new FormBuilder(PageReference.class)
                .configFields("to")
                .configSelectionProvider(pagesSelectionProvider, "to")
                .build();
        form.readFromObject(pageReference);
    }

    public PageReference getPageReference() {
        return pageReference;
    }

    public Form getForm() {
        return form;
    }

    public String getIncludeActionPath() {
        return includeActionPath;
    }

}
