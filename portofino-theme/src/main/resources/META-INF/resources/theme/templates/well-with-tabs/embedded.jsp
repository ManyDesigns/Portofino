<stripes:layout-component name="mainPageActionBody">
    <div>
        <stripes:layout-component name="pageHeader">
            <div class="pageHeader">
                <stripes:form action="${actionBean.context.actualServletPath}" method="post">
                    <h4>
                        <span class="pull-right">
                            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
                            <stripes:layout-component name="pageHeaderButtons">
                                <span class="btn-group">
                                    <portofino:buttons list="pageHeaderButtons" cssClass="btn-mini" />
                                </span>
                            </stripes:layout-component>
                        </span>
                        <stripes:layout-component name="pageTitle" />
                    </h4>
                </stripes:form>
            </div>
        </stripes:layout-component>
        <div class="pageBody">
            <stripes:layout-component name="pageBody" />
        </div>
    </div>
</stripes:layout-component>