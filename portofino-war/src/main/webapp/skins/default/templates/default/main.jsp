<!DOCTYPE html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="../../head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <title>
            <stripes:layout-component name="pageTitle">
                <c:out value="${actionBean.dispatch.lastPageInstance.page.description}"/>
            </stripes:layout-component>
        </title>
    </head>
    <body>
    <jsp:include page="../../header.jsp"/>
    <div class="container">
        <div class="row">
            <div class="span2">
                <stripes:layout-component name="sidebar">
                    <portofino:embedded-page-actions list="aboveNavigation" />
                    <div id="navigation">
                        <jsp:include page="../../navigation.jsp" />
                    </div>
                    <portofino:embedded-page-actions list="belowNavigation" />
                </stripes:layout-component>
            </div>
            <div id="content" class="span10">
                <stripes:layout-component name="mainPageAction">
                    <stripes:layout-component name="mainPageActionHeader">
                        <stripes:layout-component name="beforeSessionMessages" />
                        <mde:sessionMessages />
                        <stripes:layout-component name="beforeBreadcrumbs" />
                        <jsp:include page="/skins/${skin}/breadcrumbs.jsp" />
                        <stripes:layout-component name="afterBreadcrumbs" />
                    </stripes:layout-component>
                    <stripes:layout-component name="mainPageActionBody">
                        <div id="portlet_${actionBean.pageInstance.page.id}">
                            <stripes:layout-component name="portletHeader">
                                <div class="portletHeader" style="padding-bottom: 0;">
                                    <h3>
                                        <span class="pull-right btn-group">
                                            <stripes:form action="${actionBean.dispatch.originalPath}" method="post">
                                                <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
                                                <stripes:layout-component name="portletHeaderButtons">
                                                    <portofino:buttons list="portletHeaderButtons" cssClass="btn-mini" />
                                                </stripes:layout-component>
                                            </stripes:form>
                                        </span>
                                        <stripes:layout-component name="portletTitle" />
                                    </h3>
                                </div>
                            </stripes:layout-component>
                            <div class="portletBody">
                                <stripes:layout-component name="portletBody" />
                            </div>
                        </div>
                    </stripes:layout-component>
                    <stripes:layout-component name="mainPageActionFooter" />
                </stripes:layout-component>
                <stripes:layout-component name="embeddedPageActions">
                    <portofino:embedded-page-actions list="default" cssClass="row-fluid" />
                    <div class="row-fluid">
                        <portofino:embedded-page-actions list="contentLayoutLeft" cssClass="span6" />
                        <portofino:embedded-page-actions list="contentLayoutRight" cssClass="span6" />
                    </div>
                    <portofino:embedded-page-actions list="contentLayoutBottom" cssClass="row-fluid" />
                </stripes:layout-component>
            </div>
        </div>
    </div>
    <jsp:include page="../../footer.jsp"/>
    </body>
    </html>