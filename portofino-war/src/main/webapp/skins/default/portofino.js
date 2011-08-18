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
        }
    )
}

function confirmDeletePage(contextPath) {
    var dialogDiv = $(document.createElement("div"));
    dialogDiv.load(contextPath + "/Page.action?confirmDelete", function() {
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

function showMovePageDialog(contextPath) {
    var dialogDiv = $(document.createElement("div"));
    dialogDiv.load(contextPath + "/Page.action?chooseNewLocation", function() {
        dialogDiv.find("#dialog-move-page").dialog({
            modal: true,
            buttons: {
                "Move": function() {
                    alert("TODO");
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

    $("input:submit.wrench, button.wrench").button({
            icons: {
                primary: "ui-icon-wrench"
            },
            text: false
        });
    $("input:submit.arrow-4, button.arrow-4").button({
            icons: {
                primary: "ui-icon-arrow-4"
            },
            text: false
        });
    $("input:submit.refresh, button.refresh").button({
            icons: {
                primary: "ui-icon-refresh"
            },
            text: false
        });
    $("input:submit.link, button.link").button({
            icons: {
                primary: "ui-icon-link"
            },
            text: false
        });
    $("input:submit.person, button.person").button({
            icons: {
                primary: "ui-icon-person"
            },
            text: false
        });
    $("input:submit.plusthick, button.plusthick").button({
            icons: {
                primary: "ui-icon-plusthick"
            },
            text: false
        });
    $("input:submit.minusthick, button.minusthick").button({
            icons: {
                primary: "ui-icon-minusthick"
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
    container.prepend('<button name="cancelLayout">Cancel</button> ');
    container.prepend('<button name="updateLayout">Save</button>');
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
                theButton.after(hiddenField);
            }
        });
        return true;
    });

}
