<%@   tag import="com.manydesigns.elements.fields.Field" 
%><%@ tag import="com.manydesigns.elements.xml.XhtmlBuffer"
%><%@ attribute name="form" required="true" rtexprvalue="true" type="com.manydesigns.elements.composites.AbstractCompositeElement "
%><%@ attribute name="name" required="true" type="java.lang.String"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%
    Field field = form.findFieldByPropertyName(name);
    if(field == null) {
        throw new NoSuchFieldException("Field " + name + " not found in form");
    }
    XhtmlBuffer xb = new XhtmlBuffer(out);
    
    field.toXhtml(xb);
%>