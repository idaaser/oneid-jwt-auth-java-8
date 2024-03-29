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
public class JWT {

    public static Config newConfig(String loginBaseURL, String issuer, String privatKeyStr) throws Exception {
        PrivateKey privateKey = Utils.parsePrivateKey(privatKeyStr);

        return new Config(privateKey, loginBaseURL.trim(), issuer.trim());
    }

    /**
     * 基于用户信息, 生成一个新的id_token
     * @param config
     * @param user
     * @return
     * @throws Exception
     */
    public static String newToken(Config config, UserInfo user) throws Exception {
        validateUser(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JWTClaimNames.SUBJECT, user.getId());
        claims.put("name", user.getName());
        claims.put("preferred_username", user.getPreferredUsername());
        claims.put("email", user.getEmail());
        claims.put("phone_number", user.getMobile());
        if (user.getExtension() != null) {
            claims.putAll(user.getExtension());
        }

        return newTokenWithClaims(config, claims);
    }

    /**
     * 基于自定义的claims, 生成一个新的id_token
     * @param config
     * @param claims
     * @return
     * @throws Exception
     */
    public static String newTokenWithClaims(Config config, Map<String, Object> claims) throws Exception {
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
     * @param config
     * @param user
     * @param app
     * @return
     * @throws Exception
     */
    public static String newLoginURL(Config config, UserInfo user, String app) throws Exception {
        return newLoginURL(config, user, app, null);
    }

    /**
     * 给用户创建一个免登应用的url
     * @param config
     * @param user
     * @param app
     * @param params url params
     * @return
     * @throws Exception
     */
    public static String newLoginURL(Config config, UserInfo user, String app, Map<String, String> params) throws Exception {
        String token = newToken(config, user);
        return newLoginURLWithToken(config, token, app, params);
    }

    /**
     * 给用户创建一个免登应用的url
     * @param config
     * @param claims
     * @param app
     * @return
     * @throws Exception
     */
    public static String newLoginURLWithClaims(Config config, Map<String, Object> claims, String app) throws Exception {
        return newLoginURLWithClaims(config, claims,  app, null);
    }

    /**
     * 给用户创建一个免登应用的url
     * @param config
     * @param claims
     * @param app
     * @param params url params
     * @return
     * @throws Exception
     */
    public static String newLoginURLWithClaims(Config config, Map<String, Object> claims, String app, Map<String, String> params) throws Exception {
        String token = newTokenWithClaims(config, claims);
        return newLoginURLWithToken(config, token,  app, params);
    }

    public static String newLoginURLWithToken(Config config, String token, String app, Map<String, String> params) throws Exception {
        if(Utils.isEmpty(token)){
            throw new IllegalArgumentException("token MUST NOT be empty");
        }
        String url = config.getLoginBaseURL().replace("{app_type}", app);

        StringJoiner paramJoiner = new StringJoiner("&", "?", "");

        paramJoiner.add(Utils.urlEncode(config.getTokenParam()) + "=" + Utils.urlEncode(token));
        if(params != null && !params.isEmpty()){
            params.forEach((k,v) -> paramJoiner.add(Utils.urlEncode(k) + "=" + Utils.urlEncode(v)));
        }
        return url + paramJoiner;
    }

    /**
     * 有效性校验
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
}
