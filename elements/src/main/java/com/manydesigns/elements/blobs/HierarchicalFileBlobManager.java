package com.manydesigns.elements.blobs;

import com.manydesigns.elements.util.RandomUtil;

import java.io.File;

public class HierarchicalFileBlobManager extends FileBlobManager {

    public HierarchicalFileBlobManager(File blobsDir, String metaFileNamePattern, String dataFileNamePattern) {
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
        File dir = new File(new File(new File(blobsDir, code.substring(0, 2)), code.substring(2, 4)), code.substring(4, 6));
        if(!dir.isDirectory() && dir.mkdirs()) {
            logger.warn("Invalid blob directory: {}", dir.getAbsolutePath());
        }
        return dir;
    }
}
