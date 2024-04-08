# oneid-jwt-auth-java-8

OneID JWT auth sdk for java 8

## 使用步骤

###集成SDK
在Releases中查看和下载SDK jar文件，可以看到有2个SDK jar文件，其中一个是带有`jar-with-dependencies`的jar文件，它包含了所有的依赖项，因此您无需额外引入其他依赖。另一个是不带`jar-with-dependencies`的jar文件，它需要您额外引入其他依赖，例如`nimbus-jose-jwt`。推荐使用带`jar-with-dependencies`的jar，只需将其添加到您的项目中即可。

* 将`oneid-jwt-auth-java-8-{version}-jar-with-dependencies.jar`文件添加到项目的library中，确保路径配置正确。
* 对于maven项目，将以下内容添加到`pom.xml`文件中的`dependencies`部分，并调整version和systemPath为实际jar路径
  ```
  <dependency>
       <groupId>com.tencent.oneid</groupId>
       <artifactId>oneid-jwt-auth-java-8</artifactId>
       <version>{version}</version>
       <scope>system</scope>
       <systemPath>${project.basedir}/lib/oneid-jwt-auth-java-8-{version}-jar-with-dependencies.jar</systemPath>
  </dependency>
  ```
* 对于gradle项目，将以下内容添加到`build.gradle`文件的`dependencies`部分，并调整为实际jar路径
  ```
  implementation files('lib/oneid-jwt-auth-java-8-{version}-jar-with-dependencies.jar')
  ```
  
###使用SDK
> 使用案例参考：JWTTest.java
1. 初始化配置：JWT.newConfig(loginBaseURL, issuer, privatKeyStr)
   1. 指定id_token有效期，默认是5分钟，单位是秒：config.setTokenLifetime(time)
2. 生成免登url：
   - 通过用户信息UserInfo生成：JWT.newToken(config, userInfo)
   - 通过自定义claims生成：JWT.newTokenWithClaims(config, claims)