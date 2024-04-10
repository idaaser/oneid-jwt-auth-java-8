# oneid-jwt-auth-java-8

OneID JWT auth sdk for java
> 支持jdk1.8及以上版本，已验证通过版本：jdk1.8、jdk11、jdk17、jdk21

## 使用步骤
### 集成SDK
在Releases中查看和下载SDK jar文件，可以看到有2个SDK jar文件，其中一个是带有`jar-with-dependencies`的jar文件，它包含了所有的依赖项，因此您无需额外引入其他依赖。另一个是不带`jar-with-dependencies`的jar文件，它需要您额外引入其他依赖，例如`nimbus-jose-jwt` `bcpkix-jdk18on`。推荐使用带`jar-with-dependencies`的jar，只需将其添加到您的项目中即可。
* 将`oneid-jwt-auth-java-8-{version}-jar-with-dependencies.jar`文件添加到项目的library中，确保路径配置正确。
* 对于maven项目，将以下内容添加到`pom.xml`文件中的`dependencies`部分，并调整version和systemPath为实际jar路径
  ```xml
  <dependency>
       <groupId>com.tencent.oneid</groupId>
       <artifactId>oneid-jwt-auth-java-8</artifactId>
       <version>{version}</version>
       <scope>system</scope>
       <systemPath>${project.basedir}/lib/oneid-jwt-auth-java-8-{version}-jar-with-dependencies.jar</systemPath>
  </dependency>
  ```
* 对于gradle项目，将以下内容添加到`build.gradle`文件的`dependencies`部分，并调整为实际jar路径
  ```gradle
  implementation files('lib/oneid-jwt-auth-java-8-{version}-jar-with-dependencies.jar')
  ```
### 使用SDK
> 使用案例参考：JwtTest.java
1. 初始化配置：
  ```java
  JwtAuth jwtAuth = JwtAuth.builder()
                .loginBaseURL(testLoginBaseURL) //登录链接，JWT认证源配置
                .privateKey(testPriKey) //RSA私钥字符串，支持PKCS#1和PKCS#8格式；JWT认证源配置，可以自动生成密钥或手动生成密钥后上传公钥，Utils.generateRSAKeyPair()函数可生成RSA 2048位PKCS#8格式公私钥
                .issuer(testIssuer) //issuer JWT认证源配置
                .tokenLifetime(600) //token有效期 单位秒，不传默认为300秒，最长不能超过JWT认证源设置的Token最长有效期，否则会被认为无效
                .build();
  ```
2. 生成免登url：
- 通过用户信息UserInfo生成
```java
UserInfo userInfo = new UserInfo()
              .setId("sub")
              .setName("name")
              .setPreferredUsername("preferred_username")
              .setEmail("email")
              .setMobile("mobile")
              .setExtension(Collections.singletonMap("ext", "extData"));

String url = jwtAuth.newLoginURL(userInfo, Constants.APP_TENCENT_MEETING);
```
- 通过自定义claims生成
```java
Map<String, Object> claims = new HashMap<String, Object>() {{
          put("sub", "");
          put("name", "");
          put("preferred_username", "");
          put("email", "");
          put("phone", "");
      }};

String token = jwtAuth.newLoginURLWithClaims(claims, Constants.APP_TENCENT_MEETING);
```