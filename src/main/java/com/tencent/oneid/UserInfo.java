package com.tencent.oneid;

import java.util.Map;

/**
 * @author: marcdai
 * @date: 2024/3/27
 */
public class UserInfo {
    /**
     * 必填: 用户唯一标识， 映射到id_token中的sub
     */
    String id;
    /**
     * 建议填写: 用户显示名，映射到id_token中的name
     */
    String name;
    /**
     * 建议填写: 登录用户名，映射到id_token中的preferred_username
     */
    String preferredUsername;
    /**
     * 选填: 映射到id_token中的email
     */
    String email;
    /**
     * 选填: 映射到id_token中的mobile，登录名、邮箱、手机号建议三选一
     */
    String mobile;
    /**
     * 其他需要放到id_token里的属性
     */
    Map<String, Object> extension;

    public String getId() {
        return id;
    }

    public UserInfo setId(String id) {
        this.id = id == null ? null : id.trim();
        return this;
    }

    public String getName() {
        return name;
    }

    public UserInfo setName(String name) {
        this.name = name == null ? null : name.trim();
        return this;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public UserInfo setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername == null ? null : preferredUsername.trim();
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserInfo setEmail(String email) {
        this.email = email == null ? null : email.trim();
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public UserInfo setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
        return this;
    }

    public Map<String, Object> getExtension() {
        return extension;
    }

    public UserInfo setExtension(Map<String, Object> extension) {
        this.extension = extension;
        return this;
    }
}
