function updateSelectOptions(relName, selectionProviderIndex, methodName) {
    var selectFieldId = arguments[3 + selectionProviderIndex];

    var data = {
        relName : relName,
        selectionProviderIndex : selectionProviderIndex
    };
    data[methodName] = '';
    for (var i = 3; i < arguments.length; i++ ) {
        var currentId = arguments[i];
        var current = $(currentId);
        data[current.attr('name')] = current.attr('value');
    }

    var postUrl = stripQueryString(location.href);
    jQuery.ajax({
        type: 'POST',
        url: postUrl,
        data: data,
        success: function(responseData) {
            var options = responseData;
            if('string' === typeof(options)) {
                options = jQuery.parseJSON(options);
            }

            var selectField = $(selectFieldId);
            selectField.empty()

            for (var i = 0; i < options.length; i++) {
                var option = options[i];
                var y = document.createElement('option');
                y.value = option['v'];
                y.text = option['l'];
                y.selected = option['s'];

                selectField.append(y)
            }
            selectField.change();
        }
    });
}

function setupAutocomplete(autocompleteId, relName, selectionProviderIndex, methodName) {
    var setupArguments = arguments;
    var selectFieldId = setupArguments[4 + selectionProviderIndex];
    var autocompleteObj = $(autocompleteId);
    var selectField = $(selectFieldId);
    autocompleteObj.autocomplete({
        source: function( request, response ) {
            var data = {
                relName : relName,
                selectionProviderIndex : selectionProviderIndex,
                labelSearch : request.term
            };
            data[methodName] = '';
            for (var i = 4; i < setupArguments.length; i++ ) {
                var currentId = setupArguments[i];
                var current = $(currentId);
                data[current.attr('name')] = current.attr('value');
            }

            var postUrl = stripQueryString(location.href);
            $.ajax({
                type: 'POST',
                dataType: 'json',
                url: postUrl,
                data: data,
                success: function( responseData ) {
                    response( $.map( responseData, function( item ) {
                          return {
                                label: item.l,
                                value: item.l,
                                optionValue: item.v
                            };
                        }));
                },
                error: function(request, textStatus) {
                    alert(textStatus);
                }
            });
        },
        minLength: 1,
        select: function( event, ui ) {
            if (ui.item) {
                selectField.val(ui.item.optionValue);
            } else {
                selectField.val("");
            }
        },
        change: function(event, ui) {
            if (!ui.item) {
                selectField.val("");
            }
        },
        open: function() {
            $( this ).removeClass( "ui-corner-all" ).addClass( "ui-corner-top" );
        },
        close: function() {
            $( this ).removeClass( "ui-corner-top" ).addClass( "ui-corner-all" );
        }
    });
}

function setupDatePicker(dateFieldId, dateFormat) {
    var dateField = $(dateFieldId);
    dateField.datepicker({ dateFormat: dateFormat });
}

function stripQueryString(url) {
    var regexp = new RegExp("\\?.*");
    return url.replace(regexp, "");
}

function setupRichTextEditors() {
    $('textarea.mde-form-rich-text').each(function(index, element) {
        element = $(element);
        var conf = element.data('mdeRichTextConfig') || {
            toolbar: 'Full',
            toolbarCanCollapse: false
        };
        if(element.ckeditor) {
            element.ckeditor(conf);
        } else if(console && console.error) {
            console.error("CKEditor not loaded! Make sure that ckeditor/ckeditor.js and ckeditor/adapters/jquery.js are included in your page.");
        }
    });
}

$(function() {
    setupRichTextEditors();
});

function configureBulkEditTextField(id, checkboxName) {
    $("#" + id).focusin(function() {
        $("input[name=" + checkboxName + "]").prop("checked", true);
    })
}

function configureBulkEditField(id, checkboxName) {
    $("#" + id).change(function() {
        if($(this).val()) {
            $("input[name=" + checkboxName + "]").prop("checked", true);
        }
    })
}

