function setDisabled(div, disabled)
{
    var nodesToDisable = {button :'', input :'', optgroup :'',
        option :'', select :'', textarea :''};

    var nodes = div.getElementsByTagName('*');
    if (!nodes) return;

    var i = nodes.length;
    while (i--) {
        var node = nodes[i];
        if (node.nodeName
                && node.nodeName.toLowerCase() in nodesToDisable) {
            node.disabled = disabled;
        }
    }
}

function cleanSelect(id) {
    if (!document.getElementById || !document.getElementsByTagName) return;

    var mytable = document.getElementById(id);
    var rows = mytable.firstChild.children;
    for (var i = 0; i < rows.length; i++) {
        var row = rows[i];
        var myinput = row.children[0].firstChild;
        var mydiv = row.children[1].firstChild;
        setDisabled(mydiv, !myinput.checked);
    }
}