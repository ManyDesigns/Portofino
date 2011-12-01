import com.manydesigns.portofino.actions.CrudAction
import com.manydesigns.portofino.buttons.annotations.Button
import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution

class _1 extends CrudAction {

    void createSetup(object) {
        object.status = 1;
    }

    boolean createValidate(object) {
        Date now = new Date();
        object.created_on = now;
        object.updated_on = now;
    }
    
    boolean editValidate(object) {
        object.updated_on = new Date();
    }

    @Button(list = "crud-search", key="Hello!")
    public Resolution doSomething() {
        System.out.println("Funzioooona!");
        return new RedirectResolution(dispatch.getOriginalPath());
    }
    
}