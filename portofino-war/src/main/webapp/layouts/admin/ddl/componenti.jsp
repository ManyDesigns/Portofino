<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>

<script>
	$(function() {
		$( "#tabs" ).tabs();
	});
	</script>



<div class="demo">

<div id="tabs">
	<ul>
		<li><a href="#tabs-1">Columns</a></li>
		<li><a href="#tabs-2">Primary Key</a></li>
		<li><a href="#tabs-3">Foreign Key</a></li>
        <li><a href="#tabs-4">Annotations</a></li>
	</ul>
	<div id="tabs-1">

	</div>
	<div id="tabs-2">

	</div>
	<div id="tabs-3">

	</div>
    <div id="tabs-4">

	</div>
</div>

</div><!-- End demo -->

