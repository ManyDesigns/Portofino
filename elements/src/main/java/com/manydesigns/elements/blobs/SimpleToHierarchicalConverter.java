package com.manydesigns.elements.blobs;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SimpleToHierarchicalConverter {

    public static void main(String[] args) {
        if(args.length != 1) {
            System.err.println("Usage: SimpleToHierarchicalConverter <directory>");
            System.exit(-1);
        }
        File directory = new File(args[0]);
        if(!directory.isDirectory()) {
            System.err.println("Not a directory: " + directory);
            System.exit(-2);
        }
        System.exit(convert(directory));
    }

    public static int convert(File directory) {
        String metaFilenamePattern = "blob-{0}.properties";
        String dataFilenamePattern = "blob-{0}.data";
        SimpleBlobManager simple = new SimpleBlobManager(directory, metaFilenamePattern, dataFilenamePattern);
        HierarchicalBlobManager hierarchical = new HierarchicalBlobManager(directory, metaFilenamePattern, dataFilenamePattern);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("blob-") && name.endsWith(".properties");
            }
        });
        int converted = 0;
        for(File file : files) {
            String blobCode = file.getName().substring("blob-".length(), file.getName().length() - ".properties".length());
            Blob blob = new Blob(blobCode);
            try {
                simple.loadMetadata(blob);
                simple.openStream(blob);
                hierarchical.save(blob);
                blob.dispose();
                simple.delete(blob);
                converted++;
            } catch (Exception e) {
                System.err.println("Could not convert: " + blobCode + " because: " + e);
            }
        }
        return converted;
    }

}
