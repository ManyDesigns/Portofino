import com.manydesigns.elements.reflection.PropertyAccessor
import com.manydesigns.portofino.pageactions.m2m.ManyToManyAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.*

@RequiresPermissions(level = AccessLevel.VIEW)
@SupportsPermissions(ManyToManyAction.PERMISSION_UPDATE)
class MyManyToManyAction extends ManyToManyAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

    //Hooks

    @Override
    protected void prepareSave(Object newRelation) {}

    //Overrides

    @Override
    protected Object saveNewRelation(Object pk, PropertyAccessor onePropertyAccessor, PropertyAccessor manyPropertyAccessor) {
        return super.saveNewRelation(pk, onePropertyAccessor, manyPropertyAccessor)
    }

    @Override
    protected void deleteRelation(Object rel) {
        super.deleteRelation(rel)
    }


}