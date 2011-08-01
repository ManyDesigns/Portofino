<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@taglib prefix="mde" uri="/manydesigns-elements" %>
<stripes:layout-definition>
    <div class="portlet">
        <div class="portletHeader">
            <stripes:layout-component name="portletHeader">
                <div class="yui-g">
                    <div class="portletTitle">
                        <h1>
                        <stripes:layout-component name="portletTitle">
                            portletTitle
                        </stripes:layout-component>
                        </h1>
                    </div>
                    <div class="portletHeaderButtons">
                        <button class="wrench">Prova</button>
                    </div>
                </div>

            </stripes:layout-component>
        </div>
        <div class="portletBody">
            <stripes:layout-component name="portletBody"></stripes:layout-component>
        </div>
        <div class="portletFooter">
            <stripes:layout-component name="portletFooter"></stripes:layout-component>
        </div>
    </div>
</stripes:layout-definition>
