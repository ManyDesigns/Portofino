<stripes:layout-component name="mainPageActionBody">
    <div id="portlet_${actionBean.pageInstance.page.id}">
        <stripes:layout-component name="portletHeader">
            <div class="portletHeader">
                <h4>
                    <span class="pull-right">
                        <stripes:form action="${actionBean.dispatch.originalPath}" method="post">
                            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
                            <stripes:layout-component name="portletHeaderButtons">
                                <div class="btn-group">
                                    <portofino:buttons list="portletHeaderButtons" cssClass="btn-mini" />
                                </div>
                            </stripes:layout-component>
                        </stripes:form>
                    </span>
                    <stripes:layout-component name="portletTitle" />
                </h4>
            </div>
        </stripes:layout-component>
        <div class="portletBody">
            <stripes:layout-component name="portletBody" />
        </div>
    </div>
</stripes:layout-component>