/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.reflection.helpers.ClassAccessorManager;
import com.manydesigns.elements.text.ExpressionGenerator;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.usecases.UseCase;
import com.manydesigns.portofino.util.DummyHttpServletRequest;
import com.manydesigns.portofino.util.PkHelper;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UseCaseAction extends PortofinoAction
        implements ServletRequestAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // ServletRequestAware implementation
    //**************************************************************************
    public HttpServletRequest req;

    public void setServletRequest(HttpServletRequest req) {
        this.req = req;
    }

    //**************************************************************************
    // Web parameters
    //**************************************************************************

    public String useCaseName;
    public String pk;
    public String[] selection;
    public String searchString;
    public String cancelReturnUrl;

    //**************************************************************************
    // Web parameters setters (for struts.xml inspections in IntelliJ)
    //**************************************************************************

    public void setUseCaseName(String useCaseName) {
        this.useCaseName = useCaseName;
    }

    //**************************************************************************
    // Model metadata
    //**************************************************************************

    public UseCase useCase;
    public ClassAccessor useCaseAccessor;

    //**************************************************************************
    // Model objects
    //**************************************************************************

    public Object object;
    public List<Object> objects;


    //**************************************************************************
    // Presentation elements
    //**************************************************************************

    public TableForm tableForm;
    public Form form;
    public SearchForm searchForm;

    //**************************************************************************
    // Other objects
    //**************************************************************************

    public PkHelper pkHelper;

    public static final Logger logger =
            LogUtil.getLogger(UseCaseAction.class);

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    public String execute() {
        if (pk == null) {
            return searchFromString();
        } else {
            return read();
        }
    }

    //**************************************************************************
    // Common methods
    //**************************************************************************

    public void setupUseCase() {
        useCase = model.findUseCaseByName(useCaseName);
        Table table = model.findTableByQualifiedName(useCase.getTable());
        useCaseAccessor = ClassAccessorManager.getManager()
                .tryToInstantiateFromClass(table);
        pkHelper = new PkHelper(useCaseAccessor);
        if (useCase == null || useCaseAccessor == null) {
            throw new ModelObjectNotFoundError(useCaseName);
        }
    }

    //**************************************************************************
    // Search
    //**************************************************************************

    public String searchFromString() {
        setupUseCase();

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(useCaseAccessor);
        searchForm = searchFormBuilder.build();
        configureSearchFormFromString();

        return commonSearch();
    }

    private void configureSearchFormFromString() {
        if (searchString != null) {
            DummyHttpServletRequest dummyRequest =
                    new DummyHttpServletRequest();
            String[] parts = searchString.split(",");
            Pattern pattern = Pattern.compile("(.*)=(.*)");
            for (String part : parts) {
                Matcher matcher = pattern.matcher(part);
                if (matcher.matches()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2);
                    LogUtil.fineMF(logger, "Matched part: {0}={1}", key, value);
                    dummyRequest.setParameter(key, value);
                } else {
                    LogUtil.fineMF(logger, "Could not match part: {0}", part);
                }
            }
            searchForm.readFromRequest(dummyRequest);
        }
    }

    public String search() {
        setupUseCase();

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(useCaseAccessor);
        searchForm = searchFormBuilder.build();
        searchForm.readFromRequest(req);

        return commonSearch();
    }

    private String commonSearch() {
        searchString = searchForm.toSearchString();
        if (searchString.length() == 0) {
            searchString = null;
        }

        setupCriteria();

        String readLinkExpression = getReadLinkExpression();
        ExpressionGenerator hrefGenerator =
                ExpressionGenerator.create(readLinkExpression);
        hrefGenerator.setUrl(true);

        TableFormBuilder tableFormBuilder =
                new TableFormBuilder(useCaseAccessor)
                        .configNRows(objects.size());

        // ogni colonna chiave primaria sarà clickabile
        for (PropertyAccessor property : useCaseAccessor.getKeyProperties()) {
            tableFormBuilder.configHyperlinkGenerators(
                    property.getName(), hrefGenerator, null);
        }

        tableForm = tableFormBuilder.build();
        tableForm.setKeyGenerator(pkHelper.createPkGenerator());
        tableForm.setMode(Mode.VIEW);
        tableForm.setSelectable(true);
        tableForm.readFromObject(objects);

        return SEARCH;
    }

    private void setupCriteria() {
        Criteria criteria = context.createCriteria(useCase.getTable());
        searchForm.configureCriteria(criteria);
        criteria.sqlRestriction(useCase.getFilter());
        objects = context.getObjects(criteria);
    }


    public String getReadLinkExpression() {
        StringBuilder sb = new StringBuilder("/");
        sb.append(useCaseName);
        sb.append("/UseCase.action?pk=");
        boolean first = true;
        for (PropertyAccessor property : useCaseAccessor.getKeyProperties()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("%{");
            sb.append(property.getName());
            sb.append("}");
        }
        if (searchString != null) {
            sb.append("&searchString=");
            sb.append(Util.urlencode(searchString));
        }
        return sb.toString();
    }

    //**************************************************************************
    // Return to search
    //**************************************************************************

    public String returnToSearch() {
        setupUseCase();
        return RETURN_TO_SEARCH;
    }

    //**************************************************************************
    // Read
    //**************************************************************************

    public String read() {
        setupUseCase();
        Object pkObject = pkHelper.parsePkString(pk);

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(useCaseAccessor);
        searchForm = searchFormBuilder.build();
        configureSearchFormFromString();

        setupCriteria();

        object = context.getObjectByPk(useCaseAccessor.getName(), pkObject);
        if (!objects.contains(object)) {
            throw new Error("Object not in use case!!!");
        }
        FormBuilder formBuilder = new FormBuilder(useCaseAccessor);
        form = formBuilder.build();
        form.setMode(Mode.VIEW);
        form.readFromObject(object);

        return READ;
    }

}
