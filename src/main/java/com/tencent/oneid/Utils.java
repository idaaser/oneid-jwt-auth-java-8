package com.tencent.oneid;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author: marcdai
 * @date: 2024/3/27
 */
public class Utils {

    /**
     * userInfo有效性校验
     * @param userInfo
     */
    public static void validateUser(UserInfo userInfo) {
        if (Utils.isEmpty(userInfo.getId())) {
            throw new IllegalArgumentException("id MUST NOT be empty");
        }
        // 三者不能全为空
        if (Utils.isEmpty(userInfo.getPreferredUsername()) && Utils.isEmpty(userInfo.getEmail()) && Utils.isEmpty(userInfo.getMobile())) {
            throw new IllegalArgumentException("preferred_username/email/mobile MUST NOT all empty");
        }
    }

    /**
     * 私钥字符串转换成私钥对象，支持PKCS#1和PKCS#8格式
     * @param privateKeyStr 私钥字符串
     * @return
     */
    public static PrivateKey parsePrivateKey(String privateKeyStr){
        try {
            PEMParser reader = new PEMParser(new StringReader(privateKeyStr));
            Object object = reader.readObject();
            PrivateKeyInfo keyInfo;
            if (object instanceof PrivateKeyInfo) {
                keyInfo = (PrivateKeyInfo) object;
            } else {
                keyInfo = ((PEMKeyPair) object).getPrivateKeyInfo();
            }
            return (new JcaPEMKeyConverter()).getPrivateKey(keyInfo);
        } catch (Exception e) {
            throw new IllegalArgumentException("private key string cannot be converted into PrivateKey object: " + e.getMessage());
        }
    }

    /**
     * 生成RSA 2048 PKCS8公私钥
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
        System.out.println("PublicKey:\n-----BEGIN PUBLIC KEY-----\n" + publicKeyString + "\n-----END PUBLIC KEY-----");
        System.out.println("PrivateKey:\n-----BEGIN PRIVATE KEY-----\n" + privateKeyString + "\n-----END PRIVATE KEY-----");
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
