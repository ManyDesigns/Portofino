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

    @Override
    public Hash computeHash(HashRequest request) {
        SimpleHash result = new SimpleHash(request.getAlgorithmName());
        result.setSalt(request.getSalt());
        result.setIterations(request.getIterations());
        result.setBytes(request.getSource().getBytes());
        return result;
    }
}
