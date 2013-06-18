<div id="portlet_${actionBean.pageInstance.page.id}">
    <stripes:form action="${actionBean.dispatch.originalPath}" method="post" enctype="multipart/form-data"
                  class="${formClass != null ? formClass : 'form-horizontal'}">
        <%-- Hidden submit so that ENTER on a form executes the default action --%>
        <div class="hidden-submit"><portofino:buttons list="portlet-default-button" /></div>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
        <div class="portletHeader">
            <stripes:layout-component name="portletHeader">
                <h4>
                    <span class="pull-right btn-group">
                        <stripes:layout-component name="portletHeaderButtons">
                            <portofino:buttons list="portletHeaderButtons" cssClass="btn-mini" />
                        </stripes:layout-component>
                    </span>
                    <stripes:layout-component name="portletTitle" />
                </h4>
            </stripes:layout-component>
        </div>
        <div class="portletBody">
            <stripes:layout-component name="portletBody">
            </stripes:layout-component>
        </div>
    </stripes:form>
</div>