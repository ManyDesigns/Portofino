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

package com.manydesigns.portofino.pageactions.pagereference;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.dispatcher.RequestAttributes;
import com.manydesigns.portofino.pageactions.pagereference.configuration.PageReferenceConfiguration;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions;
import net.sourceforge.stripes.action.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding("/actions/ref")
@RequiresPermissions(level = AccessLevel.VIEW)
public class PageReferenceAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected PageReferenceConfiguration pageReferenceConfiguration;

    protected Form form;

    public static final Logger logger =
            LoggerFactory.getLogger(PageReferenceAction.class);

    @Inject(RequestAttributes.DISPATCH)
    public Dispatch dispatch;

    /*@Override
    protected void dereferencePageInstance() {
        //Do nothing. I need the raw PageReferenceInstance.
    }*/

    @DefaultHandler
    public Resolution execute() {
        return null; //TODO ripristinare

//
//        if(pageReferenceConfiguration.getToPage() == null) {
//            return forwardToPageNotConfigured();
//        } else {
//            //Never embed in this case - the referenced action will take
//            //care of it.
//            try {
//                Class<? extends ActionBean> targetActionClass =
//                        Dispatcher.getActionBeanClass(application, pageReferenceConfiguration.getToPage());
//                return new ForwardResolution(targetActionClass, getContext().getEventName());
//            } catch (ClassNotFoundException e) {
//                logger.error("Invalid action class", e);
//                return forwardToPageNotConfigured();
//            }
//            /*//Never embed in this case - the referenced action will take
//            //care of it.
//            String fwd = Dispatcher.getRewrittenPath(pageReference.getToPage());
//            return new ForwardResolution(fwd);*/
//            //throw new UnsupportedOperationException("Not yet implemented");
//        }
    }

    protected Resolution forwardToPageNotConfigured() {
        if(isEmbedded()) {
            return new ForwardResolution(PAGE_PORTLET_NOT_CONFIGURED);
        } else {
            return forwardToPortletPage(PAGE_PORTLET_NOT_CONFIGURED);
        }
    }

    public Resolution configureReferencedPage() {
        return null; //TODO ripristinare
        /*
        if(pageReferenceConfiguration.getToPage() == null) {
            SessionMessages.addErrorMessage("No referenced page specified");
            return configure();
        } else {
            try {
                Class<? extends ActionBean> targetActionClass =
                        Dispatcher.getActionBeanClass(application, pageReferenceConfiguration.getToPage());
                ForwardResolution fwd = new ForwardResolution(targetActionClass, getContext().getEventName());
                UrlBuilder cancelReturnUrlBuilder =
                    new UrlBuilder(Locale.getDefault(), dispatch.getAbsoluteOriginalPath(), true);
                cancelReturnUrlBuilder.addParameter("configure");
                cancelReturnUrlBuilder.addParameter("cancelReturnUrl", cancelReturnUrl);
                context.getRequest().setAttribute
                        ("cancelReturnUrl", cancelReturnUrlBuilder.toString());
                return fwd.addParameter("configure");
            } catch (ClassNotFoundException e) {
                logger.error("Invalid action class", e);
                return forwardToPageNotConfigured();
            }
        }*/
    }

    public Resolution configure() {
        setupConfigurationForm();
        return new ForwardResolution("/layouts/page/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution updateConfiguration() {
        setupConfigurationForm();
        form.readFromRequest(context.getRequest());
        boolean valid = form.validate();
        if(valid) {
            form.writeToObject(pageReferenceConfiguration);
            saveConfiguration(pageReferenceConfiguration);

            SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
        }
        return cancel();
    }

    protected void setupConfigurationForm() {
        //TODO ripristinare

        //Do NOT include root page: since it issues a redirect, it is impossible
        //to update the PageReference configuration.
        /*SelectionProvider pagesSelectionProvider =
                PageLogic.createPagesSelectionProvider(model.getRootPage(), false,
                                                       true, pageReferenceConfiguration);

        form = new FormBuilder(PageReferenceConfiguration.class)
                .configFields("to")
                .configSelectionProvider(pagesSelectionProvider, "to")
                .build();
        form.readFromObject(pageReferenceConfiguration);*/
    }

    public PageReferenceConfiguration getPageReferenceConfiguration() {
        return pageReferenceConfiguration;
    }

    public Form getForm() {
        return form;
    }

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        this.pageInstance = pageInstance;
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        pageReferenceConfiguration = (PageReferenceConfiguration) getPageInstance().getConfiguration();
        return null;
    }
}
