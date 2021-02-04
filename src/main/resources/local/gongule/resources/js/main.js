
function element(name){
    return $('[name="' + name + '"]');
}

function setAction(actionName){
    element('action').val(actionName);
}


