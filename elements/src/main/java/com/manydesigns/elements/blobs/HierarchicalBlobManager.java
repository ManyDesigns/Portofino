package com.manydesigns.elements.blobs;

import com.manydesigns.elements.util.RandomUtil;

import java.io.File;

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
}
