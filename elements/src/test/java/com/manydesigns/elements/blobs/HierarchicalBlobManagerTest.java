package com.manydesigns.elements.blobs;

public class HierarchicalBlobManagerTest extends SimpleBlobManagerTest {

    @Override
    protected void createManager() {
        manager = new HierarchicalBlobManager(blobsDir, META_FILE_NAME_PATTERN, DATA_FILE_NAME_PATTERN);
    }
}
