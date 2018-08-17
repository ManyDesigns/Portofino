package com.manydesigns.elements;

public interface FieldEncrypter {

  public String encrypt(String value);

  public String decrypt(String value);
}
