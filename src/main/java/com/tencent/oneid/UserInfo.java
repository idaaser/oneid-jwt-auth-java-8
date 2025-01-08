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
    private String id;
    /**
     * 建议填写: 用户显示名，映射到id_token中的name
     */
    private String name;
    /**
     * 建议填写: 登录用户名，映射到id_token中的preferred_username
     */
    private String username;
    /**
     * 选填: 映射到id_token中的email
     */
    private String email;
    /**
     * 选填: 映射到id_token中的mobile，登录名、邮箱、手机号建议三选一
     */
    private String mobile;
    /**
     * 其他需要放到id_token里的属性
     */
    private Map<String, Object> extension;

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

    public String getUsername() {
        return username;
    }

    public UserInfo setUsername(String username) {
        this.username = username == null ? null : username.trim();
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

    private UserInfo() {}

    /**
     * 构造函数
     * @param id 用户唯一标识
     * @param name 用户显示名称
     * @throws IllegalArgumentException 参数校验
     */
    public UserInfo(String id, String name) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        String trimmedId = id.trim();
        if (trimmedId.isEmpty()) {
            throw new IllegalArgumentException("id MUST NOT be empty");
        }
        this.id = trimmedId;

        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("name MUST NOT be empty");
        }
        this.name = trimmedName;
    }

    /**
     * userInfo有效性校验
     */
    public void validate() throws IllegalArgumentException {
        // 三者不能全为空
        if ((this.getUsername() == null || this.getUsername().isEmpty())
                && (this.getEmail() == null || this.getEmail().isEmpty())
                && (this.getMobile() == null || this.getMobile().isEmpty())) {
            throw new IllegalArgumentException("username/email/mobile MUST NOT all empty");
        }

    }
}
