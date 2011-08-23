<div class="yui-g">
    <div class="portletTitle">
        <h1>
        <stripes:layout-component name="portletTitle">
            <stripes:text name="title" value="${actionBean.title}"/>
        </stripes:layout-component>
        </h1>
    </div>
    <div class="portletHeaderButtons">
        <stripes:layout-component name="portletHeaderButtons"/>
    </div>
</div>
<div class="portletHeaderSeparator"></div>
<div style="margin-top: 2em;"></div>
<mde:write name="actionBean" property="pageConfigurationForm"/>