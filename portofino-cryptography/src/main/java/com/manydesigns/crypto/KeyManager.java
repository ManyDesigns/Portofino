package com.manydesigns.crypto;

import org.apache.commons.configuration.Configuration;
import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyManager {

  public static final Logger logger = LoggerFactory.getLogger(KeyManager.class);

  private static final String PROPERTY_ALG="com.manydesigns.crypto.algrorithm";
  private static final String PROPERTY_PRIVATE_KEY="com.manydesigns.crypto.private.key";
  private static final String PROPERTY_PUBLIC_KEY="com.manydesigns.crypto.public.key";
  private static final String PROPERTY_PRIVATE_KEY_DELETE="com.manydesigns.crypto.delete.key";
  private static final String PROPERTY_PASSPHRASE="com.manydesigns.crypto.passphrase";

  private static final String ASYMMETRIC_ALG="AES";
  private static final String SYMMETRIC_ALG="DES";

  private static final int PASS_MIN_LEN = 8;

  private SecretKey simmK;
  private PublicKey pbK;
  private PrivateKey prK;
  private String algo;
  private static KeyManager single;

  private KeyManager(Configuration configuration ) throws GeneralSecurityException, IOException {
    algo = configuration.getString(PROPERTY_ALG);

    if( algo==null )
      return;

    Boolean autoDelete = configuration.getBoolean(PROPERTY_PRIVATE_KEY_DELETE,false);
    String publicKeyPath =  configuration.getString(PROPERTY_PUBLIC_KEY);
    String privateKeyPath =  configuration.getString(PROPERTY_PRIVATE_KEY);
    String passphrasePath =  configuration.getString(PROPERTY_PASSPHRASE);

    if( algo.equals(ASYMMETRIC_ALG)){
      this.prK = getPrivateKey(privateKeyPath);
      this.pbK = getPublicKey(publicKeyPath);
      this.simmK = null;
    }else{
      String strK = getPassPhrase(passphrasePath);
      DESKeySpec desKeySpec = new DESKeySpec(strK.getBytes());
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SYMMETRIC_ALG);
      this.simmK = keyFactory.generateSecret(desKeySpec);
    }

    if( autoDelete && passphrasePath!=null ){
      try {
        if( algo.equals(ASYMMETRIC_ALG)) {
          logger.info("DELETING " + privateKeyPath);
          try(PrintWriter pw = new PrintWriter(privateKeyPath);) {
            pw.print("");
          }
        }
        else{
          logger.info("DELETING " + passphrasePath);
          try(PrintWriter pw = new PrintWriter(passphrasePath);) {
            pw.print("");
          }
        }
      } catch (IOException e) {
        logger.error(e.getMessage(),e);
      }
    }
  }

  public static KeyManager init(Configuration configuration) throws IOException, GeneralSecurityException {
    if (isActive())
      throw new GeneralSecurityException("Key manager already initialized");
    single = new KeyManager(configuration);
    return getInstance();
  }

  public static KeyManager getInstance() throws IOException, GeneralSecurityException {
    if(single == null)
      throw new GeneralSecurityException("Key manager not initialized");
    return single;
  }

  public static boolean isActive(){
    if(single == null)
      return false;
    return true;
  }

  public SecretKey getSimmK() {
      return this.simmK;
  }

  public PublicKey getPbKey(){
    return this.pbK;
  }

  public PrivateKey getPrKey(){
    return this.prK;
  }

  public String getAlgo(){ return this.algo; }

  private static String getKey(String filename) throws IOException {
    // Read key from file
    String strKeyPEM = "";
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(filename));
      String line;
      while ((line = br.readLine()) != null) {
        strKeyPEM += line + "\n";
      }
    }catch (IOException e) {
      e.printStackTrace();
    }finally{
      br.close();
    }

    return strKeyPEM;
  }

  private static PrivateKey getPrivateKey(String filename) throws IOException, GeneralSecurityException {
    String privateKeyPEM = getKey(filename);
    return getPrivateKeyFromString(privateKeyPEM);
  }

  private static PrivateKey getPrivateKeyFromString(String key) throws IOException, GeneralSecurityException {
    String privateKeyPEM = key;
    privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
    privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
    byte[] encoded = Base64.decodeBase64(privateKeyPEM.getBytes());
    KeyFactory kf = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    PrivateKey privKey = kf.generatePrivate(keySpec);
    return privKey;
  }

  private static PublicKey getPublicKey(String filename) throws IOException, GeneralSecurityException {
    String publicKeyPEM = getKey(filename);
    return getPublicKeyFromString(publicKeyPEM);
  }

  private static PublicKey getPublicKeyFromString(String key) throws IOException, GeneralSecurityException {
    String publicKeyPEM = key;
    publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
    publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
    byte[] encoded = Base64.decodeBase64(publicKeyPEM.getBytes());
    KeyFactory kf = KeyFactory.getInstance("RSA");
    PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(encoded));
    return pubKey;
  }

  private String getPassPhrase(String passphrasePath){
    logger.info("Retrieving passphrase");
    StringBuilder passPhrase = new StringBuilder();

    if(passphrasePath!=null) {
      BufferedReader br = null;
      try {
        br = new BufferedReader(new FileReader(passphrasePath));
        String line;
        while ((line = br.readLine()) != null) {
          passPhrase.append(line);
        }
      } catch (IOException e) {
        logger.error("getPassPhrase: " + e.getMessage(), e);
        logger.debug("prompt passphrase");
       return getInputPassphrase();
      }
    }else{
      logger.debug("passphrasePath null, prompt passphrase");
      return getInputPassphrase();
    }

    return passPhrase.toString();
  }

  private String getInputPassphrase() {
    String pass = null;
    Console console = System.console();
    if (console != null) {
      logger.debug("Reading from System.console");
      console.printf("_______________________________________________________\n\n ENTER PASSPHRASE: \n");
      pass = new String(console.readPassword());
      console.printf("_______________________________________________________\n");
    } else {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.print("_______________________________________________________\n\n ENTER PASSPHRASE: \n");
      try {
        logger.debug("Reading from System.in");
        pass = br.readLine();
        System.out.print("_______________________________________________________\n");
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      }
    }
    if( pass!=null && pass.length()<PASS_MIN_LEN){
      logger.warn("Passphrase too short, it should be at least 8 chars");
      return getInputPassphrase();
    }
    return pass;
  }
}