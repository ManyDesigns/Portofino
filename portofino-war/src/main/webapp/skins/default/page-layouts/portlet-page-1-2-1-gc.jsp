<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-render name="../content-page.jsp" >
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
    <stripes:layout-component name="pageContent">
        <stripes:layout-render name="../portal-page-content.jsp">
            <stripes:layout-component name="contentBody">
                <mde:sessionMessages/>
                <portofino:portlets list="default" />
                <div class="yui-gc">
                    <portofino:portlets list="contentLayoutLeft" cssClass="yui-u first" />
                    <portofino:portlets list="contentLayoutRight" cssClass="yui-u" />
                </div>
                <portofino:portlets list="contentLayoutBottom" />
            </stripes:layout-component>
        </stripes:layout-render>
    </stripes:layout-component>
</stripes:layout-render>