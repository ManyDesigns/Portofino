package com.manydesigns.elements;

import com.manydesigns.elements.crypto.CryptoService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFieldEncrypter implements FieldEncrypter {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFieldEncrypter.class);

    public String encrypt(String value) {
        if(StringUtils.trimToNull(value) == null){
            return value;
        }
        try {
            return CryptoService.getInstance().encrypt(value);
        } catch (Exception e) {
            logger.error("DefaultFieldEncrypter.encrypt error:"+e.getMessage(),e);
        }
        return value;
    }

    public String decrypt(String value) {
        if(StringUtils.trimToNull(value) == null){
            return value;
        }
        try {
            return CryptoService.getInstance().decrypt(value);
        } catch (Exception e) {
            logger.error("DefaultFieldEncrypter.decrypt error:"+e.getMessage(),e);
        }
        return value;
    }
}
