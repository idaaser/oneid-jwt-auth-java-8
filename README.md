# oneid-jwt-auth-java-8

oneid JWT auth sdk for java 8

## 使用步骤

> 使用案例参考：JWTTest.java

1. 初始化配置：JWT.newConfig(loginBaseURL, issuer, privatKeyStr)
   1. 指定id_token有效期，默认是5分钟，单位是秒：config.setTokenLifetime(time)
2. 生成免登url：
   - 通过用户信息UserInfo生成：JWT.newToken(config, userInfo)
   - 通过自定义claims生成：JWT.newTokenWithClaims(config, claims)