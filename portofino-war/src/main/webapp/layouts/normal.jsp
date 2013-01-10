<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %><%@
    taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@
    taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%><%@
    taglib prefix="mde" uri="/manydesigns-elements"%><%@
    taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.AbstractPageAction" /><%--
--%><stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/normal.jsp"></stripes:layout-render>