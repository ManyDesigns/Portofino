import com.manydesigns.elements.messages.SessionMessages

def createSetup(object) {
    object.status = 1;
}

def createValidate(object) {
    Date now = new Date();
    object.created_on = now;
    object.updated_on = now;
}

def editValidate(object) {
    object.updated_on = new Date();
}