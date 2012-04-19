import com.manydesigns.portofino.pageactions.m2m.ManyToManyAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions

@RequiresPermissions(level = AccessLevel.VIEW)
class MyManyToManyAction extends ManyToManyAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

    @Override
    protected void prepareSave(Object newRelation) {}

}