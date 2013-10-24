<stripes:layout-component name="mainPageActionBody">
    <div>
        <stripes:layout-component name="portletHeader">
            <div class="portletHeader">
                <stripes:form action="${actionBean.context.actualServletPath}" method="post">
                    <h4>
                        <span class="pull-right">
                            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
                            <stripes:layout-component name="portletHeaderButtons">
                                <span class="btn-group">
                                    <portofino:buttons list="portletHeaderButtons" cssClass="btn-mini" />
                                </span>
                            </stripes:layout-component>
                        </span>
                        <stripes:layout-component name="portletTitle" />
                    </h4>
                </stripes:form>
            </div>
        </stripes:layout-component>
        <div class="portletBody">
            <stripes:layout-component name="portletBody" />
        </div>
    </div>
</stripes:layout-component>