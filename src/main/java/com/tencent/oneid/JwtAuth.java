package com.tencent.oneid;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.PrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 * @author: marcdai
 * @date: 2024/3/27
 */
public class JwtAuth {
    private Config config;

    /**
     * 基于用户信息, 生成一个新的id_token
     *
     * @param user
     * @return
     * @throws Exception
     */
    public String newToken(UserInfo user) throws Exception {
        Utils.validateUser(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JWTClaimNames.SUBJECT, user.getId());
        claims.put("name", user.getName());
        claims.put("preferred_username", user.getPreferredUsername());
        claims.put("email", user.getEmail());
        claims.put("phone_number", user.getMobile());
        if (user.getExtension() != null) {
            claims.putAll(user.getExtension());
        }

        return newTokenWithClaims(claims);
    }

    /**
     * 基于自定义的claims, 生成一个新的id_token
     *
     * @param claims
     * @return
     * @throws Exception
     */
    public String newTokenWithClaims(Map<String, Object> claims) throws Exception {
        if (claims == null || claims.isEmpty()) {
            throw new IllegalArgumentException("claims MUST NOT be empty");
        }

        long now = System.currentTimeMillis();
        long exp = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(config.getTokenLifetime());
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issueTime(new Date(now))
                .expirationTime(new Date(exp))
                .issuer(config.getIssuer());

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
        JWSSigner signer = new RSASSASigner(config.getPrivateKey());
        jwt.sign(signer);
        return jwt.serialize();
    }

    /**
     * 给用户创建一个免登应用的url
     *
     * @param user
     * @param app
     * @return
     * @throws Exception
     */
    public String newLoginURL(UserInfo user, String app) throws Exception {
        return newLoginURL(user, app, null);
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
    public String newLoginURL(UserInfo user, String app, Map<String, String> params) throws Exception {
        String token = newToken(user);
        return newLoginURLWithToken(token, app, params);
    }

    /**
     * 基于自定义claims创建一个免登应用的url
     *
     * @param claims
     * @param app
     * @return
     * @throws Exception
     */
    public String newLoginURLWithClaims(Map<String, Object> claims, String app) throws Exception {
        return newLoginURLWithClaims(claims, app, null);
    }

    /**
     * 基于自定义claims创建一个免登应用的url
     *
     * @param claims
     * @param app
     * @param params url params
     * @return
     * @throws Exception
     */
    public String newLoginURLWithClaims(Map<String, Object> claims, String app, Map<String, String> params) throws Exception {
        String token = newTokenWithClaims(claims);
        return newLoginURLWithToken(token, app, params);
    }

    public String newLoginURLWithToken(String token, String app, Map<String, String> params) {
        if (Utils.isEmpty(token)) {
            throw new IllegalArgumentException("token MUST NOT be empty");
        }
        String url = config.getLoginBaseURL().replace("{app_type}", app);

        StringJoiner paramJoiner = new StringJoiner("&", "?", "");

        paramJoiner.add(Utils.urlEncode(config.getTokenParam()) + "=" + Utils.urlEncode(token));
        if (params != null && !params.isEmpty()) {
            params.forEach((k, v) -> paramJoiner.add(Utils.urlEncode(k) + "=" + Utils.urlEncode(v)));
        }
        return url + paramJoiner;
    }

    public Config getConfig() {
        return config;
    }

    public JwtAuth setConfig(Config config) {
        this.config = config;
        return this;
    }

    public static Config builder() {
        return new Config();
    }

    /**
     * JWT配置
     */
    public static class Config {
        /**
         * 私钥对象
         */
        private PrivateKey privateKey;

        /**
         * RSA私钥字符串：PKCS#1或PKCS#8格式
         */
        private String privateKeyStr;

        /**
         * 登录链接
         */
        private String loginBaseURL;

        private String issuer;

        /**
         * token有效期，单位:秒
         */
        private int tokenLifetime = 300;// 5min

        private String tokenParam = "id_token";

        public Config() {
        }

        public JwtAuth build() {
            if (Utils.isEmpty(privateKeyStr)) {
                throw new IllegalArgumentException("privateKey MUST NOT be empty");
            }
            if (Utils.isEmpty(loginBaseURL)) {
                throw new IllegalArgumentException("loginBaseURL MUST NOT be empty");
            }
            if (Utils.isEmpty(issuer)) {
                throw new IllegalArgumentException("issuer MUST NOT be empty");
            }
            privateKey = Utils.parsePrivateKey(privateKeyStr);
            return new JwtAuth().setConfig(this);
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        public Config privateKey(String privateKeyStr) {
            this.privateKeyStr = privateKeyStr;
            return this;
        }

        public String getLoginBaseURL() {
            return loginBaseURL;
        }

        public Config loginBaseURL(String loginBaseURL) {
            this.loginBaseURL = loginBaseURL == null ? null : loginBaseURL.trim();
            return this;
        }

        public String getIssuer() {
            return issuer;
        }

        public Config issuer(String issuer) {
            this.issuer = issuer == null ? null : issuer.trim();
            return this;
        }

        public int getTokenLifetime() {
            return tokenLifetime;
        }

        public Config tokenLifetime(int tokenLifetime) {
            this.tokenLifetime = tokenLifetime;
            return this;
        }

        public String getTokenParam() {
            return tokenParam;
        }

        public Config tokenParam(String tokenParam) {
            this.tokenParam = tokenParam == null ? null : tokenParam.trim();
            return this;
        }
    }
}
