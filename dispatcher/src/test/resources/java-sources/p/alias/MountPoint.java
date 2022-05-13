import com.manydesigns.portofino.dispatcher.AbstractResource;
import com.manydesigns.portofino.dispatcher.Resource;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;

public class MountPoint extends AbstractResource {

    @Override
    public Object init() {
        FileObject resourceLocation = null;
        try {
            resourceLocation = location.getParent().getParent().getChild("b");
            return getSubResource(resourceLocation, getSegment(), getResourceResolver());
        } catch (Exception e) {
            logger.error("Could not access aliased resource: " + resourceLocation, e);
            throw new WebApplicationException(404);
        }
    }
}
