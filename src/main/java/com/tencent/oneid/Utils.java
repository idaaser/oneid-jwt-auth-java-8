package com.tencent.oneid;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author: marcdai
 * @date: 2024/3/27
 */
public class Utils {



    public static PrivateKey parsePrivateKey(String privateKeyStr) throws InvalidKeySpecException, NoSuchAlgorithmException {
        privateKeyStr = privateKeyStr
                .replaceAll("\n", "")
                .replaceAll("\r", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .trim();

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 生成RSA 2048 PKCS8公私钥
     *
     * @throws NoSuchAlgorithmException
     */
    public static void generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // 将公钥转换为X509格式
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        String publicKeyString = Base64.getEncoder().encodeToString(x509EncodedKeySpec.getEncoded());

        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        String privateKeyString = Base64.getEncoder().encodeToString(pkcs8EncodedKeySpec.getEncoded());
        //
        System.out.println("Public Key:" + publicKeyString);
        System.out.println("Private Key:" + privateKeyString);
    }

    public static String urlEncode(String str){
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return URLEncoder.encode(str);
        }
    }
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        generateRSAKeyPair();
    }
}
