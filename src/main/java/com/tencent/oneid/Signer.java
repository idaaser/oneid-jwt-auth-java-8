package com.tencent.oneid;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.http.client.utils.URIBuilder;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class Signer {

    private static final int maxAllowedTokenLifetime = 300; // 5分钟

    private static final String defaultTokenKey = "id_token";

    /**
     * 私钥对象
     */
    private PrivateKey privateKey;

    /**
     * RSA私钥字符串：PKCS#1或PKCS#8格式
     */
    private String privateKeyStr;

    private String privateKeyFilePath;

    /**
     * 登录链接
     */
    private String loginBaseURL;

    private String issuer;

    /**
     * token有效期，单位:秒
     */
    private int tokenLifetime = maxAllowedTokenLifetime;// 5min

    private String tokenKey = defaultTokenKey;

    private Signer() {}

    private Signer(String issuer, String loginBaseURL) throws IllegalArgumentException {
        if (issuer == null) {
            throw new IllegalArgumentException("issuer must not be null");
        }
        String handledIssuer = issuer.trim();
        if (handledIssuer.isEmpty()) {
            throw new IllegalArgumentException("issuer must not be empty");
        }
        this.issuer = handledIssuer;

        if (loginBaseURL == null) {
            throw new IllegalArgumentException("loginBaseURL must not be null");
        }
        String trimmedLoginUrl = loginBaseURL.trim();
        if (trimmedLoginUrl.isEmpty()) {
            throw new IllegalArgumentException("loginBaseURL must not be empty");
        }

        String handledLoginUrl = trimmedLoginUrl.replace("{app_type}", "test");
        URI.create(handledLoginUrl);
        this.loginBaseURL = trimmedLoginUrl;
    }

    public static Signer newSigner(String privateKey, String issuer, String loginBaseURL) throws IllegalArgumentException {
        Signer signer = new Signer(issuer, loginBaseURL);

        if (privateKey == null) {
            throw new IllegalArgumentException("privateKey is null");
        }
       String handledPrivateKeyStr = privateKey.trim();
        if (handledPrivateKeyStr.isEmpty()) {
            throw new IllegalArgumentException("privateKey is empty");
        }
        signer.privateKeyStr = handledPrivateKeyStr;
        signer.privateKey = parsePrivateKey(new StringReader(handledPrivateKeyStr));

        return signer;
    }

    public static Signer newSignerWithKeyFile(String privateKeyFilePath, String issuer, String loginBaseURL) throws IllegalArgumentException {
        Signer signer = new Signer(issuer, loginBaseURL);

        if (privateKeyFilePath == null) {
            throw new IllegalArgumentException("privateKeyFilePath is null");
        }
        String handledKeyFile = privateKeyFilePath.trim();
        if (handledKeyFile.isEmpty()) {
            throw new IllegalArgumentException("privateKeyFilePath is empty");
        }

        try (FileReader fileReader = new FileReader(handledKeyFile)) {
            signer.privateKey = parsePrivateKey(fileReader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid key file: " + handledKeyFile);
        }

        return signer;
    }

    public void setTokenLifetime(int tokenLifetime) throws IllegalArgumentException {
        if (tokenLifetime <= 0 || tokenLifetime > maxAllowedTokenLifetime) {
            throw new IllegalArgumentException("tokenLifetime must less or equal than 300 seconds" );
        }
        this.tokenLifetime = tokenLifetime;
    }

    public void setTokenKey(String tokenKey) {
        this.tokenKey = tokenKey;
    }

    public String createToken(UserInfo user) throws Exception {
        user.validate();

        Map<String, Object> claims = new HashMap<>();
        claims.put(JWTClaimNames.SUBJECT, user.getId());
        claims.put("name", user.getName());
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            claims.put("preferred_username", user.getUsername());
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            claims.put("email", user.getEmail());
        }
        if (user.getMobile() != null && !user.getMobile().isEmpty()) {
            claims.put("phone_number", user.getMobile());
        }
        if (user.getExtension() != null) {
            claims.putAll(user.getExtension());
        }

        return newTokenWithClaims(claims);
    }

    public String createLoginURL(UserInfo user, String app) throws Exception {
        return createLoginURL(user, app, null);
    }

    /**
     * 给用户创建一个免登应用的url
     *
     * @param user
     * @param app
     * @param params extend url params
     * @return
     * @throws Exception
     */
    public String createLoginURL(UserInfo user, String app, Map<String, String> params) throws Exception {
        String token = createToken(user);
        return newLoginURLWithToken(token, app, params);
    }

    private String newLoginURLWithToken(String token, String app, Map<String, String> params) {
        String url = this.loginBaseURL.replace("{app_type}", app);
        URIBuilder builder;
        try {
            builder = new URIBuilder(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid login URL: " + url);
        }
        builder.addParameter(this.tokenKey, token);

        if (params != null) {
            params.forEach(builder::addParameter);
        }

        URI targetURI;
        try {
            targetURI = builder.build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid params");
        }

        return targetURI.toString();
    }

    private String newTokenWithClaims(Map<String, Object> claims) throws Exception {
        long now = System.currentTimeMillis();
        long exp = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.tokenLifetime);
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issueTime(new Date(now))
                .expirationTime(new Date(exp))
                .issuer(this.issuer);

        claims.forEach((k, v) -> {
            if (v != null) {
                builder.claim(k, v);
            }
        });

        JWTClaimsSet jwtClaimsSet = builder.build();
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT jwt = new SignedJWT(jwsHeader, jwtClaimsSet);
        JWSSigner signer = new RSASSASigner(this.privateKey);
        jwt.sign(signer);
        return jwt.serialize();
    }

    private static PrivateKey parsePrivateKey(Reader privateKeyReader) throws IllegalArgumentException {
        try {
            PEMParser reader = new PEMParser(privateKeyReader);
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
}
