package com.manydesigns.portofino.shiro;

import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.HashService;
import org.apache.shiro.crypto.hash.SimpleHash;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PlaintextHashService implements HashService {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    @Override
    public Hash computeHash(HashRequest request) {
        if(  request.getAlgorithmName().isPresent() && request.getSalt().isPresent() ){
            SimpleHash result = new SimpleHash(request.getAlgorithmName().get());
            result.setSalt(request.getSalt().get());
            //result.setIterations(request); //TODO
            result.setBytes(request.getSource().getBytes());
            return result;
        }

        return null; //TODO
    }

    @Override
    public String getDefaultAlgorithmName() {
        return null;
    }
}
