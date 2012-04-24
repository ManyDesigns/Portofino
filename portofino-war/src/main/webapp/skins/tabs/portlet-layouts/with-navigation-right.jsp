<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-render name="../content-page.jsp" >
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
    <stripes:layout-component name="contentBody">
        <mde:sessionMessages/>
        <portofino:portlets list="contentLayoutTop" />
        <div class="yui-ge">
            <portofino:portlets list="default" cssClass="yui-u first" />
            <div class="yui-u">
                <portofino:portlets list="contentLayoutAboveNavigation" />
                <div class="navigation">
                    <h4>Navigation</h4>
                    <jsp:include page="../navigation.jsp" />
                </div>
                <portofino:portlets list="contentLayoutBelowNavigation" />
            </div>
        </div>
        <portofino:portlets list="contentLayoutBottom" />
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
    </stripes:layout-component>
</stripes:layout-render>