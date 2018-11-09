package com.manydesigns.elements.crypto;

import javax.crypto.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CryptoService {

  //Affect resulting file size
  private String typeAlgo = "AES";
  private static CryptoService single;

  public String getTypeAlgo(){
    return typeAlgo;
  }

  public static CryptoService getInstance() {
    if (single == null)
      single = new CryptoService();
    return single;
  }

  private SecretKey getkey() throws GeneralSecurityException, IOException {
    return KeyManager.getInstance().getSimmK();
  }

  public String encrypt(String decrypted)
    throws GeneralSecurityException, IOException {
    return new String(Base64.getEncoder().encode(encrypt(decrypted.getBytes())));
  }

  public String decrypt(String encrypted)
    throws GeneralSecurityException, IOException {
    return new String(decrypt(Base64.getDecoder().decode(encrypted.getBytes())));
  }

  public byte[] encrypt(byte[] decrypted)
    throws GeneralSecurityException, IOException {
    return encrypt(decrypted, typeAlgo, getkey());
  }

  public byte[] decrypt(byte[] encrypted)
    throws GeneralSecurityException, IOException {
    return decrypt(encrypted, typeAlgo, getkey());
  }

  public InputStream encrypt(InputStream decrypted)
    throws GeneralSecurityException, IOException {
    return encrypt(decrypted, typeAlgo, getkey());
  }

  public InputStream decrypt(InputStream encrypted)
    throws GeneralSecurityException, IOException {
    return decrypt(encrypted, typeAlgo, getkey());
  }

  public static byte[] encrypt(byte[] decrypted, String algorithm, SecretKey key)
    throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(decrypted);
  }

  public static byte[] decrypt(byte[] encrypted, String algorithm, SecretKey key)
    throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.DECRYPT_MODE, key);
    return cipher.doFinal(encrypted);
  }

  public static InputStream decrypt(InputStream encryptedInputStream, String algorithm, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.DECRYPT_MODE, key);
    return new CipherInputStream(encryptedInputStream, cipher);
  }

  public static InputStream encrypt(InputStream decryptedInputStream, String algorithm, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return new CipherInputStream(decryptedInputStream, cipher);
  }

  public Long getFileSize( Long originalSize ){
    //if( typeAlgo ...
    Double value = Math.ceil(originalSize.doubleValue()/16d)*16;
    return value.longValue();
  }
}
