<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
           pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><stripes:layout-render name="/skins/default/common-simple.jsp">
        <stripes:layout-component name="bd" >
            <div id="yui-main">
                <div id="content" class="yui-b">
                    <stripes:layout-component name="content" />
                </div>
            </div>
            <div id="sidebar" class="yui-b">
                <mde:write name="navigation"/>
            </div>
            <script type="text/javascript">
                fixSideBar();
            </script>
        </stripes:layout-component>
    </stripes:layout-render>
</stripes:layout-definition>