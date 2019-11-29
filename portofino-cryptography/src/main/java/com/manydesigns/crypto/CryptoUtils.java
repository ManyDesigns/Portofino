package com.manydesigns.crypto;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;

class CryptoUtils {
     private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);
     private static final int PASS_MIN_LEN = 8;

    protected static String getKey(String filename) throws IOException {
        // Read key from file
        String strKey = "";
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                strKey += line + "\n";
            }
        } catch (IOException e) {
            logger.error("getKey from "+filename+": "+e.getMessage());
            throw e;
        }
        return strKey;
    }


     static void checkPassphrase(String pass) throws InvalidPassphraseException {
        if(  pass==null ){
            throw new InvalidPassphraseException("Passphrase is null");
        }
        if(pass.equals("")){
            throw new InvalidPassphraseException("Passphrase is empty");
        }
        if( pass.length()<PASS_MIN_LEN){
            throw new InvalidPassphraseException("Passphrase too short, it should be at least "+PASS_MIN_LEN+" chars");
        }
        if(!StringUtils.isAlphanumeric(pass)){
            throw new InvalidPassphraseException("Passphrase not alphanumeric");
        }
    }

     static void secureDeleteFile(String path) throws FileNotFoundException {
        logger.info("DELETING " + path);
        try (PrintWriter pw = new PrintWriter(path);) {
            pw.print("");
        }
        File f = new File(path);
        if (f.delete()) {
            logger.info("DELETED" + path);
        } else {
            logger.info("COULD NOT DELETE" + path);
        }
    }

    static String getStringChecksum(MessageDigest digest, String text) throws IOException {

        //Get the hash's bytes
        byte[] bytes = digest.digest(text.getBytes());

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }
}
