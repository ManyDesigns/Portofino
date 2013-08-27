import com.manydesigns.portofino.pageactions.crud.CrudAction
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import com.manydesigns.portofino.pageactions.crud.ModelSelectionProviderSupport
import com.manydesigns.portofino.model.database.DatabaseSelectionProvider
import com.manydesigns.elements.text.QueryStringWithParameters
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.PageActionsModule
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element

@SupportsPermissions([ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE ])
@RequiresPermissions(level = AccessLevel.VIEW)
class issues extends CrudAction {

    void createSetup(object) {
        object.project_id = ognlContext.project.id;
        object.lock_version = 0;
        object.done_ratio = 0;
        object.author_id = 1;
    }

    boolean createValidate(object) {
        Date now = new Date();
        object.created_on = now;
        object.updated_on = now;
        return true;
    }

    boolean editValidate(object) {
        object.updated_on = new Date();
        return true;
    }

    //Cacheable queries
    @Inject(PageActionsModule.EHCACHE_MANAGER)
    public CacheManager cacheManager;

    @Override
    protected ModelSelectionProviderSupport createSelectionProviderSupport() {
        def cacheName = "projects/issues.selectionProviders";
        cacheManager.addCacheIfAbsent(cacheName);
        return new ModelSelectionProviderSupport(this, persistence) {
            @Override
            protected void putInQueryCache(DatabaseSelectionProvider sp, QueryStringWithParameters queryWithParameters, Collection objects) {
                cacheManager.getCache(cacheName).put(new Element(queryWithParameters, objects))
            }

            @Override
            protected Collection getFromQueryCache(DatabaseSelectionProvider sp, QueryStringWithParameters queryWithParameters) {
                return (Collection) cacheManager.getCache(cacheName).get(queryWithParameters)?.value
            }

        }
    }

}