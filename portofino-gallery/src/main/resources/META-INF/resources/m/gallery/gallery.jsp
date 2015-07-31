<%@ page import="org.joda.time.DateMidnight" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="net.sourceforge.stripes.action.ActionBean" %>
<%@ page import="java.util.List" %>
<%@ page import="com.manydesigns.portofino.pageactions.gallery.Image" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.gallery.GalleryAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
<stripes:layout-component name="pageTitle">
    <c:out value="${actionBean.page.title}"/>
</stripes:layout-component>
<stripes:layout-component name="pageBody">

<script type="text/javascript" src="<stripes:url value='/m/gallery/jssor/jssor.slider.min.js' />" ></script>
<!--<script type="text/javascript" src="<stripes:url value='/m/gallery/docs.min.js' />" ></script>   -->

<!-- Jssor Slider Begin -->
<!-- To move inline styles to css file/block, please specify a class name for each element. -->
<div id="slider1_container" style="position: relative;
        width: <c:out value="${actionBean.configuration.width}"/>px;
        height: <c:out value="${actionBean.configuration.height}"/>px;
        overflow: hidden;">

    <!-- Loading Screen -->
    <div u="loading" style="position: absolute; top: 0px; left: 0px;">
        <div style="filter: alpha(opacity=70); opacity:0.7; position: absolute; display: block; background-color: white; top: 0px; left: 0px;width: 100%;height:100%;">
        </div>
    </div>

    <div u="slides" class="slider" style="cursor: move; position: absolute; left: 0px; top: 0px;
            width: <c:out value="${actionBean.configuration.width}"/>px ;
            height: <c:out value="${actionBean.configuration.height}"/>px; overflow: hidden;">
        <!--
         <div>
            <img u="image" src="../img/travel/14.jpg" />
            <img u="thumb" src="../img/travel/thumb-14.jpg" />
        </div>
        -->

        <%
            List<Image> images = actionBean.getImages();
            XhtmlBuffer xhtmlBuffer = new XhtmlBuffer(out);
            for(Image img : images) {
                xhtmlBuffer.openElement("div");
                xhtmlBuffer.openElement("img");
                xhtmlBuffer.addAttribute("src", img.getSrc());
                xhtmlBuffer.addAttribute("alt", img.getAlt());
                xhtmlBuffer.addAttribute("class", img.getClass_());
                xhtmlBuffer.addAttribute("id", "img_" + img.getId_() );
                xhtmlBuffer.addAttribute("title", img.getTitle() );
                xhtmlBuffer.addAttribute("u","image");
                xhtmlBuffer.closeElement("img");

                xhtmlBuffer.openElement("img");
                xhtmlBuffer.addAttribute("src", img.getThumbSrc() );
                xhtmlBuffer.addAttribute("alt", img.getAlt());
                xhtmlBuffer.addAttribute("class", img.getClass_());
                xhtmlBuffer.addAttribute("id", "thumb_" + img.getId_() );
                xhtmlBuffer.addAttribute("title", img.getTitle() );
                xhtmlBuffer.addAttribute("u","thumb");
                xhtmlBuffer.closeElement("img");

                //xhtmlBuffer.addAttribute("u","thumb");
                xhtmlBuffer.closeElement("div");
            }
        %>
    </div>
    <!--#region Thumbnail Navigator Skin Begin -->
    <!-- Help: http://www.jssor.com/development/slider-with-thumbnail-navigator-jquery.html -->
    <style>
            /* jssor slider thumbnail navigator skin 07 css */
            /*
            .jssort07 .p            (normal)
            .jssort07 .p:hover      (normal mouseover)
            .jssort07 .pav          (active)
            .jssort07 .pav:hover    (active mouseover)
            .jssort07 .pdn          (mousedown)
            */
        .slider{
            border: 1px solid lightgray;
            border-radius: 5px;
            box-shadow: inset 0 1px 3px rgba(0,0,0,.5),0 1px 0 rgba(155,155,155,.2);
        }

        .jssort07 {
            position: absolute;
            /* size of thumbnail navigator container */
            width: 100%;
            height: 100%;
        }

        .jssort07.bar{
            border-radius: 0px 0px 15px 15px ;
            /*border: 1px solid #ccc;*/
            background: -webkit-linear-gradient(top,  rgba(245, 245, 245, 0.15) 0%, rgba(245, 245, 245, 0) 100%);
            background: linear-gradient(to bottom,  rgba(245, 245, 245, 0.15) 0%, rgba(245, 245, 245, 0) 100%);
            /* box-shadow: 0 1px 3px rgba(0,0,0,.05),0 1px 0 rgba(255,255,255,.1);*/
        }
        .jssort07 .p {
            position: absolute;
            top: 0;
            left: 0;
            width: 99px;
            height: 66px;

        }
        .jssort07 .i {
            position: absolute;
            top: 0px;
            left: 0px;
            width: 99px;
            height: 66px;
            filter: alpha(opacity=80);
            opacity: .8;
        }
        .jssort07 .p:hover .i, .jssort07 .pav .i {
            filter: alpha(opacity=100);
            opacity: 1;
        }
        .jssort07 .o {
            position: absolute;
            top: 0px;
            left: 0px;
            width: 97px;
            height: 64px;
           /* border: 1px solid rgba(51, 122, 183, 0.3); */
            box-sizing: content-box;
           transition: border-color .6s;
            -moz-transition: border-color .6s;
            -webkit-transition: border-color .6s;
            -o-transition: border-color .6s;
        }
        .jssort07 .pav .o {
             /*border: 1px solid rgba(51, 122, 183, 0.3); */
            background: -webkit-linear-gradient(top,  rgba(250, 250, 250, 0.7) 0%, rgba(250, 250, 250, 0.2) 100%);
            background: linear-gradient(to bottom,  rgba(250, 250, 250, 0.7) 0%, rgba(250, 250, 250, 0.2) 100%);
            box-shadow: 0px 1px 3px rgba(80, 80, 80, 0.5);
        }
        .jssort07 .p:hover .o {
             border: 1px solid rgba(255, 255, 255, 0.2);
            transition: none;
            -moz-transition: none;
            -webkit-transition: none;
            -o-transition: none;
        }
        .jssort07 .p.pdn .o {
            border-color: rgba(250, 250, 250,0.3);

        }
        * html .jssort07 .o {
            /* ie quirks mode adjust */
            width /**/: 99px;
            height /**/: 66px;
        }
    </style>
    <!-- thumbnail navigator container -->
    <div u="thumbnavigator" class="jssort07 bar" style="width: <c:out value="${actionBean.configuration.width}"/>px; height: 100px; left: 0px; bottom: 0px;">
        <!-- Thumbnail Item Skin Begin -->
        <div u="slides" style="cursor: default;">
            <div u="prototype" class="p">
                <div u="thumbnailtemplate" class="i"></div>
                <div class="o"></div>
            </div>
        </div>
        <!-- Thumbnail Item Skin End -->
        <!--#region Arrow Navigator Skin Begin -->
        <!-- Help: http://www.jssor.com/development/slider-with-arrow-navigator-jquery.html -->
        <style>
                /* jssor slider arrow navigator skin 11 css */
                /*
                .jssora11l                  (normal)
                .jssora11r                  (normal)
                .jssora11l:hover            (normal mouseover)
                .jssora11r:hover            (normal mouseover)
                .jssora11l.jssora11ldn      (mousedown)
                .jssora11r.jssora11rdn      (mousedown)
                */
            .jssora11l, .jssora11r {
                display: block;
                position: absolute;
                /* size of arrow element */
                width: 37px;
                height: 37px;
                cursor: pointer;
                background: url(../img/a11.png) no-repeat;
                overflow: hidden;
            }
            .jssora11l {
                background-position: -11px -41px;
            }
            .jssora11r {
                background-position: -71px -41px;
            }
            .jssora11l:hover {
                background-position: -131px -41px;
            }
            .jssora11r:hover {
                background-position: -191px -41px;
            }
            .jssora11l.jssora11ldn {
                background-position: -251px -41px;
            }
            .jssora11r.jssora11rdn {
                background-position: -311px -41px;
            }
        </style>
        <!-- Arrow Left -->
            <span u="arrowleft" class="jssora11l" style="top: 123px; left: 8px;">
            </span>
        <!-- Arrow Right -->
            <span u="arrowright" class="jssora11r" style="top: 123px; right: 8px;">
            </span>
        <!--#endregion Arrow Navigator Skin End -->
    </div>
    <!--#endregion Thumbnail Navigator Skin End -->
    <a style="display: none" href="http://www.jssor.com">Image Slider</a>
    <!-- Trigger -->
</div>
<!-- Jssor Slider End -->
<script>
    jQuery(document).ready(function ($) {
        var options = {
            $AutoPlay:  <c:out value="${actionBean.configuration.autoplay}"/> ,                                    //[Optional] Whether to auto play, to enable slideshow, this option must be set to true, default value is false
            $AutoPlayInterval: <c:out value="${actionBean.configuration.autoplayInterval}"/> ,                            //[Optional] Interval (in milliseconds) to go for next slide since the previous stopped if the slider is auto playing, default value is 3000
            $SlideDuration: 500,                                //[Optional] Specifies default duration (swipe) for slide in milliseconds, default value is 500
            $DragOrientation: 3,                                //[Optional] Orientation to drag slide, 0 no drag, 1 horizental, 2 vertical, 3 either, default value is 1 (Note that the $DragOrientation should be the same as $PlayOrientation when $DisplayPieces is greater than 1, or parking position is not 0)
            $UISearchMode: 0,                                   //[Optional] The way (0 parellel, 1 recursive, default value is 1) to search UI components (slides container, loading screen, navigator container, arrow navigator container, thumbnail navigator container etc).
            $ThumbnailNavigatorOptions: {
                $Class: $JssorThumbnailNavigator$,              //[Required] Class to create thumbnail navigator instance
                $ChanceToShow: 2,                               //[Required] 0 Never, 1 Mouse Over, 2 Always
                $Loop: 0,                                       //[Optional] Enable loop(circular) of carousel or not, 0: stop, 1: loop, 2 rewind, default value is 1
                $SpacingX: 3,                                   //[Optional] Horizontal space between each thumbnail in pixel, default value is 0
                $SpacingY: 3,                                   //[Optional] Vertical space between each thumbnail in pixel, default value is 0
                $DisplayPieces: 6,                              //[Optional] Number of pieces to display, default value is 1
                $ParkingPosition: 253,                          //[Optional] The offset position to park thumbnail,
                $ArrowNavigatorOptions: {
                    $Class: $JssorArrowNavigator$,              //[Requried] Class to create arrow navigator instance
                    $ChanceToShow: 2,                               //[Required] 0 Never, 1 Mouse Over, 2 Always
                    $AutoCenter: 2,                                 //[Optional] Auto center arrows in parent container, 0 No, 1 Horizontal, 2 Vertical, 3 Both, default value is 0
                    $Steps: 6                                       //[Optional] Steps to go for each navigation request, default value is 1
                }
            }
        };
        var jssor_slider1 = new $JssorSlider$("slider1_container", options);
        //responsive code begin
        //you can remove responsive code if you don't want the slider scales while window resizes
        function ScaleSlider() {
            var parentWidth = jssor_slider1.$Elmt.parentNode.clientWidth;
            if (parentWidth)
                jssor_slider1.$ScaleWidth(Math.min(parentWidth, <c:out value="${actionBean.configuration.width}"/>));
            else
                window.setTimeout(ScaleSlider, 30);
        }
        ScaleSlider();
        $(window).bind("load", ScaleSlider);
        $(window).bind("resize", ScaleSlider);
        $(window).bind("orientationchange", ScaleSlider);
        //responsive code end
    });
</script>
<!--#endregion Jssor Slider End -->
</stripes:layout-component>
</stripes:layout-render>