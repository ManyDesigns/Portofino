<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
$(function() {
    var form = $(".crud-search-form");

    form.find("button[name=bulkDelete]").click(function() {
        return confirm ('<fmt:message key="commons.confirm" />');
    });

    form.each(function(i, form) {
        form = $(form);
        form.find(".search_form_toggle_link").click(makeToggleFunction(form));
    });

    function makeToggleFunction(form) {
        var visible = form.data("search-visible");
        return function(event) {
            $(this).next().slideToggle(300);
            visible = !visible;
            if(visible) {
                $(event.target).html('<fmt:message key="layouts.crud.search.hideSearch" />');
            } else {
                $(event.target).html('<fmt:message key="layouts.crud.search.showSearch" />');
            }
            return false;
        };
    }
});