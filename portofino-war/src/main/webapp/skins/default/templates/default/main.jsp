<!DOCTYPE html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="../../head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <title><c:out value="${actionBean.dispatch.lastPageInstance.page.description}"/></title>
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
                        <div class="row-fluid"><mde:sessionMessages /></div>
                        <div class="pull-right">

                        </div>
                        <jsp:include page="/skins/${skin}/breadcrumbs.jsp" />
                    </stripes:layout-component>
                    <stripes:layout-component name="mainPageActionBody">
                        <div id="portlet_${actionBean.pageInstance.page.id}">
                            <stripes:form action="${actionBean.dispatch.originalPath}" method="post" enctype="multipart/form-data"
                                          class="${formClass != null ? formClass : 'form-horizontal'}">
                                <%-- Hidden submit so that ENTER on a form executes the default action --%>
                                <div class="hidden-submit"><portofino:buttons list="portlet-default-button" /></div>
                                <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
                                <stripes:layout-component name="portletHeader">
                                    <h3 style="border-bottom: 1px solid #E5E5E5">
                                        <div class="pull-right btn-group">
                                            <stripes:layout-component name="portletHeaderButtons">
                                                <portofino:buttons list="portletHeaderButtons" />
                                            </stripes:layout-component>
                                        </div>
                                        <stripes:layout-component name="portletTitle" />
                                    </h3>
                                </stripes:layout-component>
                                <stripes:layout-component name="portletBody" />
                            </stripes:form>
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