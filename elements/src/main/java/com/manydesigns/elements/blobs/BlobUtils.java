package com.manydesigns.elements.blobs;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.FormElement;
import com.manydesigns.elements.fields.FileBlobField;
import com.manydesigns.elements.forms.FieldSet;
import com.manydesigns.elements.forms.Form;

import java.io.IOException;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class BlobUtils {

    public static void loadBlobs(Form form, BlobManager blobManager) {
        for(FieldSet fieldSet : form) {
            loadBlobs(fieldSet, blobManager);
        }
    }

    public static void loadBlobs(FieldSet fieldSet, BlobManager blobManager) {
        for(FormElement field : fieldSet) {
            if(FileBlobField.class.isInstance(field)) {
                FileBlobField fbf = FileBlobField.class.cast(field);
                Blob blob = fbf.getValue();
                if(blob != null && !blob.isPropertiesLoaded()) {
                    try {
                        blobManager.loadMetadata(blob);
                        fbf.setBlobError(null);
                    } catch (IOException e) {
                        fbf.setBlobError(ElementsThreadLocals.getText("elements.error.field.fileblob.cannotLoad"));
                    }
                }
            }
        }
    }

    public static void saveBlobs(Form form, BlobManager blobManager) throws IOException {
        for(FieldSet fieldSet : form) {
            saveBlobs(fieldSet, blobManager);
        }
    }

    public static void saveBlobs(FieldSet fieldSet, BlobManager blobManager) throws IOException {
        for(FormElement field : fieldSet) {
            if(FileBlobField.class.isInstance(field)) {
                FileBlobField fbf = FileBlobField.class.cast(field);
                Blob blob = fbf.getValue();
                if(blob != null) {
                    blobManager.save(blob);
                }
            }
        }
    }

}
