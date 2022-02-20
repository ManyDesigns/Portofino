package com.manydesigns.elements.crypto;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyManager {

    public static final Logger logger = LoggerFactory.getLogger(KeyManager.class);

    private static final String PROPERTY_ALG = "com.manydesigns.crypto.algorithm";
    private static final String PROPERTY_PRIVATE_KEY = "com.manydesigns.crypto.private.key";
    private static final String PROPERTY_PUBLIC_KEY = "com.manydesigns.crypto.public.key";
    private static final String PROPERTY_PRIVATE_KEY_DELETE = "com.manydesigns.crypto.delete.key";
    private static final String PROPERTY_PASSPHRASE = "com.manydesigns.crypto.passphrase";
    private static final String PROPERTY_SECURITY_LOCATION = "com.manydesigns.crypto.location";

    private static final String ASYMMETRIC_ALG = "ASIM";
    private static final String SYMMETRIC_ALG = "SIM";

    private SecretKey simmK;
    private PublicKey pbK;
    private PrivateKey prK;
    private String algo;
    private static KeyManager single;

    private KeyManager(Configuration configuration) throws GeneralSecurityException, IOException, InvalidPassphraseException, InvalidSettingsException {
        algo = configuration.getString(PROPERTY_ALG);

        if (algo == null) {
            logger.warn("No " + PROPERTY_ALG + " defined, KeyManager will not be initialized ");
            return;
        }

        boolean autoDelete = configuration.getBoolean(PROPERTY_PRIVATE_KEY_DELETE, false);
        String securityLocation = configuration.getString(PROPERTY_SECURITY_LOCATION);
        String publicKeyPath = securityLocation + "/" + configuration.getString(PROPERTY_PUBLIC_KEY);
        String privateKeyPath = securityLocation + "/" + configuration.getString(PROPERTY_PRIVATE_KEY);
        String passphrasePath = securityLocation + "/" + configuration.getString(PROPERTY_PASSPHRASE);

        if( StringUtils.trimToNull(securityLocation)==null )
            throw new InvalidSettingsException("Required property "+PROPERTY_SECURITY_LOCATION+" is invalid or empty");

        if (algo.equals(ASYMMETRIC_ALG)) {
            this.prK = getPrivateKey(privateKeyPath);
            this.pbK = getPublicKey(publicKeyPath);
            this.simmK = null;
            checkOrSaveHash(privateKeyPath, this.prK.toString());
            if (autoDelete)
                CryptoUtils.secureDeleteFile(privateKeyPath);
        } else {
            if (algo.equals(SYMMETRIC_ALG)) {
                String strK = getPassPhrase(passphrasePath);
                checkOrSaveHash(passphrasePath, strK);
                CryptoUtils.checkPassphrase(strK);

                byte[] key = strK.getBytes(StandardCharsets.UTF_8);
                MessageDigest sha = MessageDigest.getInstance("SHA-1");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16); // use only first 128 bit

                this.simmK = new SecretKeySpec(key, "AES");

                if (autoDelete)
                    CryptoUtils.secureDeleteFile(passphrasePath);
            } else {
                throw new InvalidSettingsException(algo + " not supported, possible values are: " + SYMMETRIC_ALG + " | " + ASYMMETRIC_ALG);
            }
        }
    }

    private void checkOrSaveHash(String path, String key) throws IOException, GeneralSecurityException, InvalidPassphraseException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        String checksum = System.getenv("PORTOFINO_PASSPHRASE_SUM");
        if (checksum == null) {
            digest.update(path.getBytes(StandardCharsets.UTF_8));
        }

        String calculatedChecksum = CryptoUtils.getStringChecksum(digest, key);

        if (checksum == null) {
            File checksumFile = new File(path + ".sum");
            if (checksumFile.exists()) {
                checksum = StringUtils.trimToEmpty(CryptoUtils.getKey(checksumFile.getAbsolutePath()));
            } else {
                try (PrintWriter pw = new PrintWriter(checksumFile);) {
                    pw.print(calculatedChecksum);
                }
            }
        }

        if (!calculatedChecksum.equals(checksum)) {
            throw new InvalidPassphraseException("Checksum test failed, passphrase differs from last one used");
        }
    }

    public static KeyManager init(Configuration configuration) throws IOException, GeneralSecurityException, InvalidPassphraseException, InvalidSettingsException {
        if (isActive())
            throw new GeneralSecurityException("Key manager already initialized");
        single = new KeyManager(configuration);
        return getInstance();
    }

    public static KeyManager getInstance() throws GeneralSecurityException {
        if (single == null)
            throw new GeneralSecurityException("Key manager not initialized");
        return single;
    }

    public static boolean isActive() {
        return single != null;
    }

    public SecretKey getSimmK() {
        return this.simmK;
    }

    public PublicKey getPbKey() {
        return this.pbK;
    }

    public PrivateKey getPrKey() {
        return this.prK;
    }

    public String getAlgo() {
        return this.algo;
    }

    private static PrivateKey getPrivateKey(String filename) throws IOException, GeneralSecurityException {
        String privateKeyPEM = CryptoUtils.getKey(filename);
        return getPrivateKeyFromString(privateKeyPEM);
    }

    private static PrivateKey getPrivateKeyFromString(String key) throws IOException, GeneralSecurityException {
        String privateKeyPEM = key;
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM.getBytes());
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return kf.generatePrivate(keySpec);
    }

    private static PublicKey getPublicKey(String filename) throws IOException, GeneralSecurityException {
        String publicKeyPEM = CryptoUtils.getKey(filename);
        return getPublicKeyFromString(publicKeyPEM);
    }

    private static PublicKey getPublicKeyFromString(String key) throws IOException, GeneralSecurityException {
        String publicKeyPEM = key;
        publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
        publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM.getBytes());
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new X509EncodedKeySpec(encoded));
    }

    private String getPassPhrase(String passphrasePath) throws IOException, InvalidPassphraseException {
        logger.info("Retrieving passphrase");
        StringBuilder passPhrase = new StringBuilder();

        String passPhraseEnv = System.getenv("PORTOFINO_PASSPHRASE");
        if (passPhraseEnv == null) {
            try (BufferedReader br = new BufferedReader(new FileReader(passphrasePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    passPhrase.append(line);
                }
            } catch (IOException e) {
                logger.error("getPassPhrase: " + e.getMessage(), e);
                throw e;
            }
            return passPhrase.toString();
        } else {
            return passPhraseEnv;
        }
    }
}
