package com.tencent.oneid;

import java.security.PrivateKey;

/**
 * @author: marcdai
 * @date: 2024/3/27
 */
public class Config {
    private PrivateKey privateKey;

    private String loginBaseURL;

    private String issuer;

    private int tokenLifetime = 300;// 5min

    private String tokenParam = "id_token";

    public Config(PrivateKey privateKey, String loginBaseURL, String issuer) {
        this.privateKey = privateKey;
        this.loginBaseURL = loginBaseURL;
        this.issuer = issuer;
    }

    public Config(PrivateKey privateKey, String loginBaseURL, String issuer, int tokenLifetime, String tokenParam) {
        this.privateKey = privateKey;
        this.loginBaseURL = loginBaseURL;
        this.issuer = issuer;
        this.tokenLifetime = tokenLifetime;
        this.tokenParam = tokenParam;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public Config setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public String getLoginBaseURL() {
        return loginBaseURL;
    }

    public Config setLoginBaseURL(String loginBaseURL) {
        this.loginBaseURL = loginBaseURL == null ? null : loginBaseURL.trim();
        return this;
    }

    public String getIssuer() {
        return issuer;
    }

    public Config setIssuer(String issuer) {
        this.issuer = issuer == null ? null : issuer.trim();
        return this;
    }

    public int getTokenLifetime() {
        return tokenLifetime;
    }

    public Config setTokenLifetime(int tokenLifetime) {
        this.tokenLifetime = tokenLifetime;
        return this;
    }

    public String getTokenParam() {
        return tokenParam;
    }

    public Config setTokenParam(String tokenParam) {
        this.tokenParam = tokenParam == null ? null : tokenParam.trim();
        return this;
    }
}
