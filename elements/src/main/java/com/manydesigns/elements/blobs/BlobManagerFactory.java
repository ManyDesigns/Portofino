package com.manydesigns.elements.blobs;

public interface BlobManagerFactory {
    BlobManager getBlobManager();
    boolean accept(String type);
}
