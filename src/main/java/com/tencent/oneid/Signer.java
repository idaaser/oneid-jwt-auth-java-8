package com.tencent.oneid;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.hc.core5.net.URIBuilder;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Signer {

    private static final int maxAllowedTokenLifetime = 300; // 5分钟

    private static final String defaultTokenKey = "id_token";

    /**
     * 私钥对象
     */
    private PrivateKey privateKey;

    /**
     * OneID JWT认证源页面提供的登录链接
     */
    private String loginBaseURL;

    /**
     * 发起登录方的应用标识
     */
    private String issuer;

    /**
     * token有效期，单位:秒
     */
    private int tokenLifetime = maxAllowedTokenLifetime;// 5min

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

    /**
     * 初始化JWT认证签发器
     *
     * @param privateKey 私钥
     * @param issuer 发起登录方的应用标识
     * @param loginBaseURL OneID JWT认证源页面提供的登录链接
     * @throws IllegalArgumentException 参数错误
     */
    public static Signer newSigner(String privateKey, String issuer, String loginBaseURL) throws IllegalArgumentException {
        Signer signer = new Signer(issuer, loginBaseURL);

        if (privateKey == null) {
            throw new IllegalArgumentException("privateKey is null");
        }
       String handledPrivateKeyStr = privateKey.trim();
        if (handledPrivateKeyStr.isEmpty()) {
            throw new IllegalArgumentException("privateKey is empty");
        }
        signer.privateKey = parsePrivateKey(new StringReader(handledPrivateKeyStr));

        return signer;
    }

    /**
     * 初始化JWT认证签发器
     *
     * @param keyFilePath 私钥文件路径
     * @param issuer 发起登录方的应用标识
     * @param loginBaseURL OneID JWT认证源页面提供的登录链接
     * @throws IllegalArgumentException 参数错误
     */
    public static Signer newSignerWithKeyFile(String keyFilePath, String issuer, String loginBaseURL) throws IllegalArgumentException {
        Signer signer = new Signer(issuer, loginBaseURL);

        if (keyFilePath == null) {
            throw new IllegalArgumentException("keyFilePath is null");
        }
        String handledKeyFile = keyFilePath.trim();
        if (handledKeyFile.isEmpty()) {
            throw new IllegalArgumentException("keyFilePath is empty");
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

    private String createToken(UserInfo user) throws Exception {
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

    /**
     * 为指定用户创建一个免登应用的url
     *
     * @param user 免登用户的信息
     * @param app 免登应用的唯一标识
     * @return 免登链接
     * @throws Exception 签发凭证时，可能
     */
    public String newLoginURL(UserInfo user, String app) throws Exception {
        return newLoginURL(user, app, null);
    }

    /**
     * 为指定用户创建一个免登应用的url
     *
     * @param user 免登用户的信息
     * @param app 免登应用的唯一标识
     * @param params 表示自定义的key/value键值对(以query param的方式追加到免登链接之后)
     * @return 免录链接
     * @throws Exception 签发凭证时，可能
     */
    public String newLoginURL(UserInfo user, String app, Map<String, String> params) throws Exception {
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
        builder.addParameter(defaultTokenKey, token);

        if (params != null) {
            params.forEach((k, v) -> {
                if (k != null && !k.isEmpty() && v != null && !v.isEmpty()) {
                    builder.addParameter(k, v);
                }
            });
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

        UUID uuid = UUID.randomUUID();
        String hexUUID = uuid.toString().replace("-", "");
        builder.jwtID(hexUUID);

        claims.forEach((k, v) -> {
            if (k != null && !k.isEmpty()) {
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
