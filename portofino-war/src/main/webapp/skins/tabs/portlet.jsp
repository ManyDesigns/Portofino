<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<%@taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-definition>
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
    <div class="portlet" id="portlet_${actionBean.pageInstance.page.id}">
        <stripes:form action="${actionBean.dispatch.originalPath}" method="post" enctype="multipart/form-data">
            <%-- Hidden submit so that ENTER on a form executes the default action --%>
            <div class="hidden-submit"><portofino:buttons list="portlet-default-button" /></div>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
            <div class="portletHeader">
                <stripes:layout-component name="portletHeader">
                    <div>
                        <div class="portletTitle">
                            <h1>
                            <stripes:layout-component name="portletTitle">
                            </stripes:layout-component>
                            </h1>
                        </div>
                        <div class="portletHeaderButtons">
                            <stripes:layout-component name="portletHeaderButtons">
                                <portofino:buttons list="portletHeaderButtons" />
                            </stripes:layout-component>
                        </div>
                    </div>
                    <div class="portletHeaderSeparator"></div>
                </stripes:layout-component>
            </div>
            <div class="portletBody">
                <stripes:layout-component name="portletBody">
                </stripes:layout-component>
            </div>
            <div class="portletFooter">
                <stripes:layout-component name="portletFooter">
                </stripes:layout-component>
            </div>
        </stripes:form>
    </div>
</stripes:layout-definition>
