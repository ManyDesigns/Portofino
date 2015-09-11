import com.manydesigns.elements.Mode
import com.manydesigns.elements.annotations.FileBlob
import com.manydesigns.elements.annotations.LabelI18N
import com.manydesigns.elements.blobs.Blob
import com.manydesigns.elements.blobs.BlobManager
import com.manydesigns.elements.fields.FileBlobField
import com.manydesigns.elements.forms.Form
import com.manydesigns.elements.forms.FormBuilder
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.buttons.annotations.Buttons
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.model.database.Database
import com.manydesigns.portofino.model.database.DatabaseLogic
import com.manydesigns.portofino.model.database.Table
import com.manydesigns.portofino.modules.BaseModule
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.reflection.TableAccessor
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageOutputStream
import org.apache.commons.lang.StringUtils
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.annotation.RequiresAuthentication
import net.sourceforge.stripes.action.*
import org.apache.commons.io.IOUtils

@RequiresPermissions(level = AccessLevel.VIEW)
public class Profile extends CustomAction {

    @Inject(DatabaseModule.PERSISTENCE)
    public Persistence persistence;

    @Inject(BaseModule.DEFAULT_BLOB_MANAGER)
    protected BlobManager blobManager;

    protected Form form;
    protected Map user;
    protected String avatar;

    public int MAX_WIDTH = 40, MAX_HEIGHT = 40;

    public static String[] VIEW_FIELDS = [
            "email",
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

    @DefaultHandler
    public Resolution view() {
        //Setup form for view
        Table usersTable = DatabaseLogic.findTableByEntityName(getDatabase(), "users");
        TableAccessor tableAccessor = new TableAccessor(usersTable);
        form = new FormBuilder(tableAccessor).
                configFields(VIEW_FIELDS).
                configMode(Mode.VIEW).
                build();
        loadUser();
        form.readFromObject(user);
        return new ForwardResolution("/jsp/profile/view.jsp");
    }

    protected Map loadUser() {
        user = SecurityUtils.subject.principal;
        avatar = user.avatar;
        return user;
    }

    protected def setupPhotoForm() {
        form = new FormBuilder(getClass()).configFields("avatar").build()
    }

    public Resolution photo() {
        if(StringUtils.isEmpty(avatar)) {
            return new RedirectResolution("/images/user-placeholder-40x40.png");
        } else {
            loadUser()
            Blob blob = new Blob(user.avatar);
            blobManager.loadMetadata(blob);
            InputStream inputStream = blobManager.openStream(blob);
            return new StreamingResolution(blob.contentType, inputStream);
        }
    }

    @Button(list = "view", order = 1D, type = Button.TYPE_PRIMARY, key = "change.your.password", icon=Button.ICON_GEAR)
    public Resolution changePassword() {
        return new RedirectResolution("/login").
                addParameter("changePassword").
                addParameter("returnUrl", context.actionPath).
                addParameter("cancelReturnUrl", context.actionPath);
    }

    @RequiresAuthentication
    @Button(list = "view", order = 2D, type = Button.TYPE_DEFAULT, key = "update.your.data" , icon=Button.ICON_EDIT)
    public Resolution editData() {
        setupEditForm();
        return new ForwardResolution("/jsp/profile/update-data.jsp");
    }

    protected def setupEditForm() {
        Table usersTable = DatabaseLogic.findTableByEntityName(getDatabase(), "users");
        TableAccessor tableAccessor = new TableAccessor(usersTable);
        form = new FormBuilder(tableAccessor).
                configFields(EDIT_FIELDS).
                configMode(Mode.EDIT).
                build();
        loadUser();
        form.readFromObject(user)
    }

    @RequiresAuthentication
    @Button(list = "view", order = 3D, type = Button.TYPE_DANGER, key = "change.your.photo" , icon = Button.ICON_PICTURE)
    public Resolution changePhoto() {
        loadUser();
        setupPhotoForm();
        return new ForwardResolution("/jsp/profile/upload-photo.jsp");
    }

    @RequiresAuthentication
    @Button(list = "upload-photo", order = 1D, type = Button.TYPE_PRIMARY, key = "upload")
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
    }

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

    @RequiresAuthentication
    @Button(list = "upload-photo", order = 2D, key = "delete.current.photo" , icon=Button.ICON_TRASH)
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

    @RequiresAuthentication
    @Button(list = "update-data", order = 1D, key = "update", type = Button.TYPE_PRIMARY)
    public Resolution updateData() {
        setupEditForm();
        form.readFromRequest(context.request);
        if(form.validate()) {
            form.writeToObject(user);
            def session = persistence.getSession("tt")
            session.update("users", (Object) user);
            session.transaction.commit();
            return new RedirectResolution(context.actionPath);
        } else {
            return new ForwardResolution("/jsp/profile/update-data.jsp");
        }
    }

    @Button(list = "view", order = 4D, type = Button.TYPE_INFO, key = "notifications" , icon=Button.ICON_COMMENT )
    public Resolution notifications() {
        return new RedirectResolution("/profile/notifications");
    }

    @Buttons([
        @Button(list = "upload-photo", order = 3D, key = "cancel"),
        @Button(list = "update-data", order = 2D, key = "cancel"),
    ])
    public Resolution cancel() {
        return new RedirectResolution(context.actionPath);
    }

    protected Database getDatabase() {
        return persistence.getModel().getDatabases().find { d -> d.databaseName.equals("tt") };
    }

    public Form getForm() {
        return form;
    }

    public Map getUser() {
        return user;
    }

    @LabelI18N("upload.a.new.photo")
    @FileBlob
    String getAvatar() {
        return avatar;
    }

    void setAvatar(String photo) {
        this.avatar = photo;
    }
}