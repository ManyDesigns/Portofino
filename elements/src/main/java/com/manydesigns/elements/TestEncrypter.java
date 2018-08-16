package com.manydesigns.elements;

public class TestEncrypter implements FieldEncrypter {

  public String encrypt(String value) {
    return value!=null?"Encrypted": null;
  }

  public String decrypt(String value) {
    return value!=null?"Decrypted": null;
  }
}
