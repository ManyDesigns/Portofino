package portofino.actions.profile

import com.manydesigns.elements.Mode
import com.manydesigns.elements.blobs.Blob
import com.manydesigns.elements.blobs.BlobManager
import com.manydesigns.elements.fields.FileBlobField
import com.manydesigns.elements.forms.Form
import com.manydesigns.elements.forms.FormBuilder
import com.manydesigns.portofino.database.model.Database
import com.manydesigns.portofino.database.model.DatabaseLogic
import com.manydesigns.portofino.database.model.Table
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.reflection.TableAccessor
import com.manydesigns.portofino.resourceactions.custom.CustomAction
import com.manydesigns.portofino.rest.FormParametersAccessor
import com.manydesigns.portofino.rest.Utilities
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageOutputStream
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
import java.awt.*
import java.awt.image.BufferedImage

@RequiresPermissions(level = AccessLevel.VIEW)
public class Profile extends CustomAction {

    @Autowired
    public Persistence persistence;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.DEFAULT_BLOB_MANAGER)
    protected BlobManager blobManager;

    protected Form form;

    public int MAX_WIDTH = 40, MAX_HEIGHT = 40;

    public static String[] VIEW_FIELDS = [
            "email",
            "first_name",
            "last_name",
            "registration",
            "registration_ip",
            "last_access",
            "last_access_ip",
            "validated",
            "admin",
            "project_manager",
    ]

    public static String[] EDIT_FIELDS = [
            "first_name",
            "last_name"
    ]

    @GET
    Response view() {
        //Setup form for view
        def session = persistence.getSession("tt")
        def user = session.load('users', SecurityUtils.subject.principal.id)
        userAsJson(user)
    }

    protected Response userAsJson(user) {
        def usersTable = DatabaseLogic.findTableByEntityName(getDatabase(), "users")
        def tableAccessor = new TableAccessor(usersTable)
        form = new FormBuilder(tableAccessor).
                configFields(VIEW_FIELDS).
                configMode(Mode.VIEW).
                build()
        form.readFromObject(user)
        Response.ok(form).build()
    }

    protected def setupPhotoForm() {
        form = new FormBuilder(getClass()).configFields("avatar").build()
    }

    @GET
    @Path("photo")
    public Response photo() {
        def user = SecurityUtils.subject.principal
        if(StringUtils.isEmpty(user.avatar)) {
            context.servletContext.getResourceAsStream("/images/user-placeholder-40x40.png").withStream {
                return Response.ok(IOUtils.toByteArray(it), "image/png").build()
            }
        } else {
            Blob blob = new Blob(user.avatar)
            return Utilities.downloadBlob(blob, blobManager, context.request, logger);
        }
    }
    protected def setupEditForm() {
        Table usersTable = DatabaseLogic.findTableByEntityName(getDatabase(), "users");
        TableAccessor tableAccessor = new TableAccessor(usersTable);
        form = new FormBuilder(tableAccessor).
                configFields(EDIT_FIELDS).
                configMode(Mode.EDIT).
                build();
        def user = SecurityUtils.subject.principal
        form.readFromObject(user)
    }

    /*
    @RequiresAuthentication
    public Resolution changePhoto() {
        loadUser();
        setupPhotoForm();
        return new ForwardResolution("/jsp/profile/upload-photo.jsp");
    }

    @RequiresAuthentication
    public Resolution uploadPhoto() {
        setupPhotoForm();
        form.readFromRequest(context.request);
        form.writeToObject(this);
        if(avatar != null) {
            Blob blob = scaleAndCropAvatar();
            loadUser();
            if(user.avatar != null) {
                blobManager.delete(new Blob(user.avatar));
            }
            user.avatar = blob.code;
            def session = persistence.getSession("tt")
            session.update("users", (Object) user);
            logger.debug("Save user: {} avatar: {}", user.id, user.avatar);
            session.transaction.commit();
        }
        return new RedirectResolution(context.actionPath);
    }*/

    protected Blob scaleAndCropAvatar() {
        FileBlobField field = (FileBlobField) form.findFieldByPropertyName("avatar");
        def blob = field.getValue();
        def image = ImageIO.read(blob.getInputStream());
        blob.dispose();
        double scaleXFactor = ((double) MAX_WIDTH) / image.width;
        double scaleYFactor = ((double) MAX_HEIGHT) / image.height;
        double scaleFactor = Math.max(scaleXFactor, scaleYFactor);
        if (scaleFactor < 1) {
            BufferedImage imageBuff = scaleImage(image, scaleFactor);
            if (imageBuff.width > MAX_WIDTH || imageBuff.height > MAX_HEIGHT) {
                int x = Math.max((int) ((imageBuff.width - MAX_WIDTH) / 2), 0);
                int y = Math.max((int) ((imageBuff.height - MAX_HEIGHT) / 2), 0);
                int w = Math.min(imageBuff.width, MAX_WIDTH);
                int h = Math.min(imageBuff.height, MAX_HEIGHT);
                imageBuff = imageBuff.getSubimage(x, y, w, h);
            }

            //Save scaled&cropped image
            def writers = ImageIO.getImageWritersByMIMEType(field.getValue().contentType);
            def writer;
            if (writers.hasNext()) {
                writer = writers.next();
            } else {
                writer = ImageIO.getImageWritersByFormatName("png").next();
                field.getValue().setContentType("image/png");
            }
            def stream = new ByteArrayOutputStream()
            writer.output = new MemoryCacheImageOutputStream(stream);
            writer.write(imageBuff);
            writer.dispose();
            blob.setInputStream(new ByteArrayInputStream(stream.toByteArray()));
            blobManager.save(blob);
            stream.close();
        }
        return blob
    }

    protected BufferedImage scaleImage(BufferedImage image, double scaleFactor) {
        if(scaleFactor < 0.5) {
            image = scaleImage(image, (double) scaleFactor * 2.0);
            scaleFactor = 0.5;
        }
        int newWidth = image.width * scaleFactor;
        int newHeight = image.height * scaleFactor;
        BufferedImage imageBuff = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        def g2d = imageBuff.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        return imageBuff;
    }
/*
    @RequiresAuthentication
    public Resolution deletePhoto() {
        loadUser();
        if(user.avatar != null) {
            blobManager.delete(user.avatar);
            user.avatar = null;
            def session = persistence.getSession("tt")
            session.update("users", (Object) user);
            session.transaction.commit();
        }
        return new RedirectResolution(context.actionPath);
    }
    */

    @RequiresAuthentication
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @PUT
    Response updateData(MultivaluedMap<String, String> formValues) {
        setupEditForm()
        form.readFrom(new FormParametersAccessor(formValues))
        if(form.validate()) {
            def session = persistence.getSession("tt")
            def user = session.load('users', SecurityUtils.subject.principal.id)
            form.writeToObject(user)
            session.update("users", (Object) user)
            session.transaction.commit()
            userAsJson(user)
        } else {
            Response.serverError().entity(form).build()
        }
    }

    protected Database getDatabase() {
        return persistence.getDatabases().find { d -> d.databaseName.equals("tt") };
    }

    public Form getForm() {
        return form;
    }


}
