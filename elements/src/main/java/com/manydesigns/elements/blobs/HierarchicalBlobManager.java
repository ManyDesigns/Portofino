package com.manydesigns.elements.blobs;

import com.manydesigns.elements.util.RandomUtil;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class HierarchicalBlobManager extends SimpleBlobManager {

    public HierarchicalBlobManager(File blobsDir, String metaFileNamePattern, String dataFileNamePattern) {
        super(blobsDir, metaFileNamePattern, dataFileNamePattern);
    }

    @Override
    protected File getMetaFile(String code) {
        File dir = getBlobSubdir(code);
        return RandomUtil.getCodeFile(dir, metaFileNamePattern, code);
    }

    @Override
    protected File getDataFile(String code) {
        File dir = getBlobSubdir(code);
        return RandomUtil.getCodeFile(dir, dataFileNamePattern, code);
    }

    protected File getBlobSubdir(String code) {
        return new File(new File(new File(blobsDir, code.substring(0, 2)), code.substring(2, 4)), code.substring(4, 6));
    }

    @Override
    public InputStream openStream(Blob blob) throws IOException {
        ensureValidCode(blob.getCode());

        if( blob.getRepository() != null ) { //TODO vedere se usare così
            Repository repository = RepositoryRegistry.getInstance().getRepository(blob.getRepository());
            repository.load(blob);
        }else{
            if(blob.isEncrypted())
                blob.setInputStream(BlobUtils.decrypt(new FileInputStream(getDataFile(blob.getCode())),blob.getEncryptionType()));
            else
                blob.setInputStream(new FileInputStream(getDataFile(blob.getCode())));
        }
        return blob.getInputStream();
    }

    @Override
    public void save(Blob blob) throws IOException {
        if( blob.getRepository() != null ){ //TODO vedere se usare così
            ensureValidCode(blob.getCode());

            Repository repository=RepositoryRegistry.getInstance().getRepository(blob.getRepository());
            repository.save(blob);

            File metaFile = getMetaFile(blob.getCode());
            if(!metaFile.getParentFile().isDirectory()) {
                metaFile.getParentFile().mkdirs();
            }
            FileOutputStream out = new FileOutputStream(metaFile);
            try {
                blob.getMetaProperties().store(out, "Remote Blob code #" + blob.getCode()); //TODO aggiungere altre info in commento
            } finally {
                IOUtils.closeQuietly(out);
            }
            blob.dispose();
        }else{
            super.save(blob);
        }
    }

    @Override
    public boolean delete(Blob blob) {
       if( !blob.isPropertiesLoaded() ){
           try{ loadMetadata(blob); }
           catch (IOException e){
               logger.warn("Cound load metadata ", e);
           }
       }
        if( blob.getRepository() != null ){
            String code = blob.getCode();
            ensureValidCode(code);
            File metaFile = getMetaFile(code);
            boolean success = true;
            try {
                Repository repository=RepositoryRegistry.getInstance().getRepository(blob.getRepository());
                success = repository.delete(blob) && success;
            } catch (Exception e) {
                logger.warn("Cound not delete file from repository ", e);
                success = false;
            }
            try {
                success = metaFile.delete() && success;
            } catch (Exception e) {
                logger.warn("Cound not delete meta file", e);
                success = false;
            }
            return success;
        }else {
            return super.delete(blob);
        }
    }
}
