package com.guavapay.api.util;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CommonUtil {

    public static PublicKey getPublicKeyFromString(String key) throws GeneralSecurityException {
        byte[] encoded = Base64.getDecoder().decode(key.replaceAll("\n", ""));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new X509EncodedKeySpec(encoded));
    }
}
