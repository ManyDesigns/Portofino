/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.servlet.AttributeMap;
import com.manydesigns.elements.servlet.ElementsFilter;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PersistenceManagerListener implements ServletContextListener {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(PersistenceManagerListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        ElementsThreadLocals.setupDefaultElementsContext();
        ElementsThreadLocals.setServletContext(servletContext);
        Persistence persistence =
                (Persistence) servletContext.getAttribute(DatabaseModule.PERSISTENCE);
        AttributeMap servletContextAttributeMap = AttributeMap.createAttributeMap(servletContext);
        ElementsThreadLocals.getOgnlContext().put(
                ElementsFilter.SERVLET_CONTEXT_OGNL_ATTRIBUTE,
                servletContextAttributeMap);
        try {
            persistence.loadXmlModel();
        } finally {
            ElementsThreadLocals.removeElementsContext();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Persistence persistence =
                (Persistence) servletContextEvent.getServletContext().getAttribute(DatabaseModule.PERSISTENCE);
        persistence.shutdown();
    }
}
