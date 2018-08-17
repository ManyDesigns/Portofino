package com.manydesigns.elements.blobs;

import com.manydesigns.crypto.CryptoService;
import com.manydesigns.elements.FormElement;
import com.manydesigns.elements.fields.AbstractBlobField;
import com.manydesigns.elements.forms.FieldSet;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.TableForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class BlobUtils {

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(BlobUtils.class);

    public static void loadBlobs(Form form, BlobManager blobManager, boolean loadContents) {
        for(FieldSet fieldSet : form) {
            loadBlobs(fieldSet, blobManager, loadContents);
        }
    }

    public static void loadBlobs(FieldSet fieldSet, BlobManager blobManager, boolean loadContents) {
        for(FormElement field : fieldSet) {
            loadBlob(field, blobManager, loadContents);
        }
    }

    public static void loadBlobs(TableForm form, BlobManager blobManager, boolean loadContents) {
        for(TableForm.Row row : form.getRows()) {
            loadBlobs(row, blobManager, loadContents);
        }
    }

    public static void loadBlobs(TableForm.Row row, BlobManager blobManager, boolean loadContents) {
        for(FormElement field : row) {
            loadBlob(field, blobManager, loadContents);
        }
    }

    public static void loadBlob(FormElement field, BlobManager blobManager, boolean loadContents) {
        if(AbstractBlobField.class.isInstance(field)) {
            AbstractBlobField blobField = AbstractBlobField.class.cast(field);
            blobField.loadBlob(blobManager, loadContents);
        }
    }

    public static void saveBlobs(Form form, BlobManager blobManager) throws IOException {
        for(FieldSet fieldSet : form) {
            saveBlobs(fieldSet, blobManager);
        }
    }

    public static void saveBlobs(FieldSet fieldSet, BlobManager blobManager) throws IOException {
        for(FormElement field : fieldSet) {
            if(AbstractBlobField.class.isInstance(field)) {
                AbstractBlobField blobField = AbstractBlobField.class.cast(field);
                Blob blob = blobField.getValue();
                if(blob != null && blob.getCode() != null && blob.getInputStream() != null) {
                    blobManager.save(blob);
                }
            }
        }
    }

    public static InputStream encrypt( InputStream decryptedInputStream, String type ){
        try {
            logger.debug("Decrypting encryptedInputStream");
            return CryptoService.getInstance().encrypt(decryptedInputStream);
        } catch (GeneralSecurityException | IOException e) {
            logger.error(e.getMessage(),e);
        }
        return decryptedInputStream;
    }

    public static InputStream decrypt( InputStream encryptedInputStream , String type ){
        try {
            logger.debug("Decrypting encryptedInputStream");
            return  CryptoService.getInstance().decrypt(encryptedInputStream);
        } catch (GeneralSecurityException | IOException e) {
            logger.error(e.getMessage(),e);
        }
        return encryptedInputStream;
    }

    public static Long calculatePaddingSize(Blob blob) {
        logger.debug("Calculating padding size");
        return CryptoService.getInstance().getFileSize(blob.getSize());
    }
}
