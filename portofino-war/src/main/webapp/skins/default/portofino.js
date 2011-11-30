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

function confirmDeletePage(pageId, contextPath) {
    var dialogDiv = $(document.createElement("div"));
    dialogDiv.load(contextPath + "/actions/page?confirmDelete&pageId=" + pageId, function() {
        dialogDiv.find("#dialog-confirm-delete-page").dialog({
            modal: true,
            buttons: {
                "Delete": function() {
                    var form = $("#contentHeaderForm");
                    var hiddenField = document.createElement("input");
                    hiddenField.setAttribute("type", "hidden");
                    hiddenField.setAttribute("name", "deletePage");
                    form.append(hiddenField);
                    form.submit();
                    $(this).dialog("close");
                },
                Cancel: function() {
                    $(this).dialog("close");
                    dialogDiv.remove();
                }
            }
        });
    });
    return false;
}

function showMovePageDialog(pageId, contextPath) {
    var dialogDiv = $(document.createElement("div"));
    dialogDiv.load(contextPath + "/actions/page?chooseNewLocation&pageId=" + pageId, function() {
        dialogDiv.find("#dialog-move-page").dialog({
            modal: true,
            width: 500,
            buttons: {
                "Move": function() {
                    var form = $("#contentHeaderForm");

                    var hiddenField = document.createElement("input");
                    hiddenField.setAttribute("type", "hidden");
                    hiddenField.setAttribute("name", "movePage");
                    form.append(hiddenField);

                    hiddenField = document.createElement("input");
                    hiddenField.setAttribute("type", "hidden");
                    hiddenField.setAttribute("name", "destinationPageId");
                    hiddenField.setAttribute("value", $(this).find("#destinationPageId").val());
                    form.append(hiddenField);

                    form.submit();
                    $(this).dialog("close");
                },
                Cancel: function() {
                    $(this).dialog("close");
                    dialogDiv.remove();
                }
            }
        });
    });
    return false;
}

function showCopyPageDialog(pageId, contextPath) {
    var dialogDiv = $(document.createElement("div"));
    dialogDiv.load(contextPath + "/actions/page?copyPageDialog&pageId=" + pageId, function() {
        dialogDiv.find("#dialog-copy-page").dialog({
            modal: true,
            width: 500,
            buttons: {
                "Copy": function() {
                    var form = $("#contentHeaderForm");

                    $(this).find("input, select").each(function(index, elem) {
                        elem = $(elem);
                        var hiddenField = document.createElement("input");
                        hiddenField.setAttribute("type", "hidden");
                        hiddenField.setAttribute("name", elem.attr('name'));
                        hiddenField.setAttribute("value", elem.val());
                        form.append(hiddenField);
                    });

                    form.submit();
                    $(this).dialog("close");
                },
                Cancel: function() {
                    $(this).dialog("close");
                    dialogDiv.remove();
                }
            }
        });
    });
    return false;
}

$(function() {
    $("input:submit.contentButton, button.contentButton").button();
    
    $("input:submit.portletButton, button.portletButton").button();

    $(".portletHeaderButtons button[name=configure]").button({
            icons: {
                primary: "ui-icon-wrench"
            },
            text: false
        });
    $("button.arrow-4").button({
            icons: {
                primary: "ui-icon-arrow-4"
            },
            text: false
        });
    $("button.refresh").button({
            icons: {
                primary: "ui-icon-refresh"
            },
            text: false
        });
    $(".portletHeaderButtons button[name=manageAttachments]").button({
            icons: {
                primary: "ui-icon-link"
            },
            text: false
        });
    $("button.person").button({
            icons: {
                primary: "ui-icon-person"
            },
            text: false
        });
    $("button.copy").button({
            icons: {
                primary: "ui-icon-copy"
            },
            text: false
        });
    $("button.plusthick").button({
            icons: {
                primary: "ui-icon-plusthick"
            },
            text: false
        });
    $("button.minusthick").button({
            icons: {
                primary: "ui-icon-minusthick"
            },
            text: false
        });
    $("button.transferthick-e-w").button({
            icons: {
                primary: "ui-icon-transferthick-e-w"
            },
            text: false
        });
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
    container.prepend('<button name="cancel" class="contentButton">Cancel</button> ');
    container.prepend('<button name="updateLayout" class="contentButton">Save</button>');
    container.children("button[name=cancel]").button();
    container.children("button[name=updateLayout]").button();
    $("button[name=updateLayout]").click(function() {
        var theButton = $(this);
        $('div.portletContainer').each( function(index, element) {
            var wrapper = $(element);
            var elements = wrapper.sortable('toArray');
            for(var index in elements) {
                var hiddenField = document.createElement("input");
                hiddenField.setAttribute("type", "hidden");
                hiddenField.setAttribute("name", "portletWrapper_" + element.id);
                hiddenField.setAttribute("value", elements[index].substring("portletWrapper_".length));
                hiddenField.setAttribute("class", "updateLayout");
                theButton.before(hiddenField);
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
    return (string + '').replace(/[&<>"'\/`]/g, function (match) {
        return HTML_CHARS[match];
    });
}

