<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.map.MapAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">

        <script type="text/javascript" src="<stripes:url value='/m/map/leaflet/leaflet.js' />" ></script>
        <link rel="stylesheet" type="text/css" href="<stripes:url value='/m/map/leaflet/leaflet.css' />">

        <div class="row-fluid">
            <div class="span12">
                <div class="map leaflet-container leaflet-fade-anim" style="height: 480px; position: relative;" id="map"></div>
            </div>
        </div>

        <script>
            // create a map in the "map" div, set the view to a given place and zoom
            var map = L.map('map').setView([45, 9], 8);
            var json = { geolocation:false , lat:45 , lon:8 , zoom:8 , markers:[] };

            function onLocationError(e) {
                alert(e.message);
                map.setView([json.lat,json.lon],json.zoom);
            }

            map.on('locationerror', onLocationError);

            // add an OpenStreetMap tile layer
            L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a target="_blank" href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            }).addTo(map);

            $.get("?getJsonMarkers", function(data, status){
                // console.log("Markers found: " + data.markers.length + "\nStatus: " + status);
                json = data;

                for( var i in  data.markers){
                    var popup = '<a target="_blank" href="'+ data.markers[i].url +'" <strong> '+data.markers[i].title+'</strong></a> <br> '+data.markers[i].description ;
                    L.marker([ data.markers[i].lat , data.markers[i].lon ]).addTo(map).bindPopup( popup );
                }

                if( json.geolocation ){
                    map.locate({setView: true, maxZoom: json.zoom});
                }else{
                    map.setView([json.lat,json.lon],json.zoom);
                }
            });

        </script>
    </stripes:layout-component>
</stripes:layout-render>