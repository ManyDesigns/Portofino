<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<div>
    <div class="portletTitle">
        <h1>
        <stripes:layout-component name="portletTitle">
            <input type="text" name="title" value="<c:out value="${actionBean.title}"/>"/>
        </stripes:layout-component>
        </h1>
    </div>
    <div class="portletHeaderButtons">
        <stripes:layout-component name="portletHeaderButtons"></stripes:layout-component>
    </div>
</div>
<div class="portletHeaderSeparator"></div>
<div style="margin-top: 2em; clear: both;"></div>
<mde:write name="actionBean" property="pageConfigurationForm"/>