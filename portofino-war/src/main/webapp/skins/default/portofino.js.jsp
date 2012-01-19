<%@page contentType="text/javascript; UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

function fixSideBar() {
    $(
        function() {
            var contentNode = $('#content');
            var sideBarNode = $('#sidebar');
            var contentOffsetHeight = contentNode.attr('offsetHeight');
            var sideBarOffsetHeight = sideBarNode.attr('offsetHeight');
            if (contentOffsetHeight < sideBarOffsetHeight) {
                contentNode.css('min-height', sideBarOffsetHeight + 'px')
            }

            contentNode = $('div.search_results.withSearchForm');
            sideBarNode = contentNode.parent().parent();
            contentOffsetHeight = contentNode.attr('offsetHeight');
            sideBarOffsetHeight = sideBarNode.attr('offsetHeight');
            if (contentOffsetHeight < sideBarOffsetHeight) {
                contentNode.css('min-height', sideBarOffsetHeight + 'px')
            }
        }
    )
}

function copyFormAsHiddenFields(source, form) {
    source.find("input, select").each(function(index, elem) {
        elem = $(elem);
        var hiddenField = document.createElement("input");
        hiddenField.setAttribute("type", "hidden");
        hiddenField.setAttribute("name", elem.attr('name'));
        hiddenField.setAttribute("value", elem.val());
        form.append(hiddenField);
    });
}

function confirmDeletePage(pageId, contextPath) {
    var dialogDiv = $(document.createElement("div"));
    dialogDiv.load(contextPath + "/actions/admin/page/dialog?confirmDelete&pageId=" + pageId, function() {
        dialogDiv.find("#dialog-confirm-delete-page").dialog({
            modal: true,
            width: 500,
            buttons: {
                '<fmt:message key="commons.delete" />': function() {
                    var form = $("#pageAdminForm");
                    copyFormAsHiddenFields($(this), form);
                    form.submit();
                    $(this).dialog("close");
                },
                '<fmt:message key="commons.cancel" />': function() {
                    $(this).dialog("close");
                    dialogDiv.remove();
                }
            }
        });
    });
    return false;
}

function showMovePageDialog(pagePath, contextPath) {
    var dialogDiv = $(document.createElement("div"));
    dialogDiv.load(contextPath + "/actions/admin/page/dialog?chooseNewLocation&pagePath=" + pagePath, function() {
        dialogDiv.find("#dialog-move-page").dialog({
            modal: true,
            width: 500,
            buttons: {
                '<fmt:message key="commons.move"/>': function() {
                    var form = $("#pageAdminForm");
                    copyFormAsHiddenFields($(this), form);
                    form.submit();
                    $(this).dialog("close");
                },
                '<fmt:message key="commons.cancel" />': function() {
                    $(this).dialog("close");
                    dialogDiv.remove();
                }
            }
        });
    });
    return false;
}

function showCopyPageDialog(pagePath, contextPath) {
    var dialogDiv = $(document.createElement("div"));
    dialogDiv.load(contextPath + "/actions/admin/page/dialog?copyPageDialog&pagePath=" + pagePath, function() {
        dialogDiv.find("#dialog-copy-page").dialog({
            modal: true,
            width: 500,
            buttons: {
                '<fmt:message key="commons.copy"/>': function() {
                    var form = $("#pageAdminForm");
                    copyFormAsHiddenFields($(this), form);
                    form.submit();
                    $(this).dialog("close");
                },
                '<fmt:message key="commons.cancel" />': function() {
                    $(this).dialog("close");
                    dialogDiv.remove();
                }
            }
        });
    });
    return false;
}

$(function() {
    var decorateButton = function(selector, options) {
        $(selector).each(function(index, element) {
            element = $(element);
            element.html(element.find('.ui-button-text').html());
            element.button(options);
        });
    };

    var decorateIconButton = function(selector, icon) {
        decorateButton(selector, {
            icons: {
                primary: icon
            },
            text: false
        });
    };

    decorateButton("button.contentButton");
    decorateButton("button.portletButton");

    decorateIconButton("button.arrow-4", "ui-icon-arrow-4");
    decorateIconButton("button.refresh", "ui-icon-refresh");
    decorateIconButton("button.person", "ui-icon-person");
    decorateIconButton("button.copy", "ui-icon-copy");
    decorateIconButton("button.plusthick", "ui-icon-plusthick");
    decorateIconButton("button.minusthick", "ui-icon-minusthick");
    decorateIconButton("button.transferthick-e-w", "ui-icon-transferthick-e-w");
    decorateIconButton(".portletHeaderButtons button[name=configure]", "ui-icon-wrench");
    decorateIconButton(".portletHeaderButtons button[name=manageAttachments]", "ui-icon-link");
});

function enablePortletDragAndDrop(button) {
    $("div.portletContainer").sortable({
        connectWith: "div.portletContainer",
        placeholder: "portletPlaceholder",
        cursor: "move", // cursor image
        revert: true, // moves the portlet to its new position with a smooth transition
        tolerance: "pointer" // mouse pointer overlaps the droppable
    }).disableSelection().addClass("portletBox");

    var container = $(button).parent();
    $(button).remove();
    container.prepend('<button name="cancel" type="submit" class="contentButton">Cancel</button> ');
    container.prepend('<button name="updateLayout" type="submit" class="contentButton">Save</button>');
    container.children("button[name=cancel]").button();
    container.children("button[name=updateLayout]").button();
    $("button[name=updateLayout]").click(function() {
        var theButton = $(this);
        $('div.portletContainer').each(function(index, element) {
            var wrapper = $(element);
            var templateHiddenField = wrapper.children("input[type=hidden]").first();
            var elements = wrapper.sortable('toArray');
            console.log(templateHiddenField);
            for(var e in elements) {
                var id = elements[e];
                var hiddenField = document.createElement("input");
                hiddenField.setAttribute("type", "hidden");
                hiddenField.setAttribute("name", templateHiddenField.val());
                hiddenField.setAttribute("value", id.substring("portletWrapper_".length));
                theButton.before(hiddenField);
                console.log(hiddenField);
            }
        });
        return true;
    });

}


var HTML_CHARS = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#x27;',
    '/': '&#x2F;',
    '`': '&#x60;'
};

function htmlEscape (string) {
    if(string == null) {
        return string;
    }
    return (string + '').replace(/[&<>"'\/`]/g, function (match) {
        return HTML_CHARS[match];
    });
}

