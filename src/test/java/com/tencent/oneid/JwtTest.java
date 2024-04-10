package com.tencent.oneid;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: marcdai
 * @date: 2024/3/28
 */
public class JwtTest {
    String testLoginBaseURL = "https://oauth2.eid-6.account.tencentcs.com/v1/sso/jwtp/1055078342287425536/1168815965601792000/kit/{app_type}";
    String testIssuer = "https://www.example.com";
    String testPriKey = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCHzx3qF7bp3t3m+54jNYqo5IE08DeQesckiVOpKIgey6UHu/fUwdKZ4Hm+Cr+rbPLzUPFmx+yujVDIom06gNBypg6dwF09v7sod5Yt4f7MJ/E9Hqa+yjo0/l/Ei1fIzuhI762SKTuAo5QcSpJkewBXIZPyA1hdQZG+n1G5WKxyRu+xw+c+NrH57gXbA44UEg8zXacV5l78G/hqR9QEFLR0cjluqx5EKCkpGAYr2aU1/QAy2ap6LY/7IhlXuAbNcSrmEB773d/Pk5SpuBB6DoPTV1FOD4O7yhAR5kwyuHYHj1R/rVTgcexx3Fe0GoODb19EoQ2QYZz1tpwG6Qb2qzYNAgMBAAECggEAIEerP1U4KZIK0ehsL8e958N6b5QKTPnV5DoM69fhtNooJXS45TXVojQQE8r3wF4CyDEs08aA3ANkWG/9AOsVwK52KmpK706nhGiaRlkhaDK1PRcQQKiOGcL9jdil+qmtXgq0CZnp1ftrjKbv8VioHc2yHdPMOMxJQVMaiHDKelXH3VL8xY0/QIQKmdnbplWPYZuAerPBt+88oofCve17G/ZMmcSmRCxfYrPtY2DRswuF5IVeArzm+2cSpJm0xHSVOtYM6UZS6w+f0xct8QVZzR/ikS2PhMxAeiWOqi4E/2Gs6IVb9cTow5U6/yERwnMuzN7ooNnechazNPrsUvi2eQKBgQDAClr/9IKJnaCX0HJLtpvFubnnCMUsAbCMePzdCJgnszsXlDMtAC1YqK5etamITNFe6y6AyRD+shuRQjOmWbAUv265M+jMiGB15SOD93WTJh+Z6YB0VN6ke/5N9rKzDkoB0c+T+LISB3LHojionyGk702Cqe8pEfx/5gTESb+TeQKBgQC1CmQ0sDdMOmCdaRwtfpxMyK2H/7+TuSwg0kmK+C35HFHqNcoVkD2Po3t4sW0BKwCU4jt4o98DfWigb80NExg/GiojedmNk0Xj076wlpOeTfpvInVSKQDaq5YlQwhkbNZnVGPKl1SpEWRyQaF76O9WgLN3DTUNvT5t5N0OqKWeNQKBgEqrPBrNbXwop+qfh/FcfVDyGBB23oNv3L+hw2AGGXNGQzG10/gpW3hfjf0RUWvHwpjPhPNaOpttkT53qTGDKe3HSTwFrPzqAeeVQqvrKfIfHqZvnhydazw1YfnadD0ezzPJ6pY0Wrib4MsyjikyRpik21R00qL7dVwdibjwm+axAoGACmwvuqzOcjAGMenEOoZIf08qVmMN53ZIqRcSLtL86pOLz70LBlTIhoV17Uvhp6iPHwMrQ1XD2BKXPG1TU8Zepfteo643LSRmaKhfGRGCLCH3IgDM2k8MAorQWRLT39w4N7ivChHOSPF8Y+uNXXHanZNJQbhb7o+PkYoUg+yHpGUCgYEAobCDw/VyXOFhtkTIbBqZn4Jo3CJQKkQoQP9QXynBI7U+AUmePb+rwHsEBmERggMZma1KN3y3p1rYtueJiULHzPmigSm6Gcd2cFPDMS0nVR+thITA91nDhzc0uskgd2rUZph+OTe8jA2jOijIFLGGcNx+lVxXYCapz6ViFFYLfzE=\n" +
            "-----END PRIVATE KEY-----";

    JwtAuth jwtAuth;

    /**
     * 初始化jwt auth 配置
     */
    @Before
    public void newConfig() {
        jwtAuth = JwtAuth.builder()
                .loginBaseURL(testLoginBaseURL) //登录链接，JWT认证源配置
                .privateKey(testPriKey) //RSA私钥字符串，支持PKCS#1和PKCS#8格式；JWT认证源配置，可以自动生成密钥或手动生成密钥后上传公钥，Utils.generateRSAKeyPair()函数可生成RSA 2048位PKCS#8格式公私钥
                .issuer(testIssuer) //issuer JWT认证源配置
                .tokenLifetime(600) //token有效期 单位秒，不传默认为300秒，最长不能超过JWT认证源设置的Token最长有效期，否则会被认为无效
                .build();
    }

    /**
     * 根据userInfo生成jwt id_token
     * @throws Exception
     */
    @Test
    public void newTokenWithUserInfo() throws Exception {
        UserInfo userInfo = new UserInfo()
                .setId("f99530d4-8317-4900-bd02-0127bb8c44de")
                .setName("张三")
                .setPreferredUsername("zhangsan")
                .setEmail("zhangsan@example.com")
                .setMobile("+86 13211111111")
                .setExtension(Collections.singletonMap("picture", "https://www.example.com/avatar1.png"));

        String token = jwtAuth.newToken(userInfo);
        System.out.println(token);
    }

    /**
     * 基于自定义的claims, 生成一个jwt id_token
     * @throws Exception
     */
    @Test
    public void newTokenWithClaims() throws Exception {
        Map<String, Object> claims = new HashMap<String, Object>() {{
            put("sub", "f99530d4-8317-4900-bd02-0127bb8c44de");
            put("name", "张三");
            put("preferred_username", "zhangsan");
            put("email", "zhangsan@example.com");
            put("phone", "13211111111");
        }};

        String token = jwtAuth.newTokenWithClaims(claims);
        System.out.println(token);
    }

    /**
     * 基于用户信息创建一个免登应用的url
     * @throws Exception
     */
    @Test
    public void newLoginURL() throws Exception {
        UserInfo userInfo = new UserInfo()
                .setId("f99530d4-8317-4900-bd02-0127bb8c4422")
                .setName("李四")
                .setPreferredUsername("lisi")
                .setEmail("lisi@example.com")
                .setMobile("+86 13211111111")
                .setExtension(Collections.singletonMap("picture", "https://www.example.com/avatar1.png"));

        String url = jwtAuth.newLoginURL(userInfo, Constants.APP_TENCENT_MEETING);
        System.out.println(url);
    }

    /**
     * 基于自定义claims创建一个免登应用的url
     * @throws Exception
     */
    @Test
    public void newLoginURLWithClaims() throws Exception {
        Map<String, Object> claims = new HashMap<String, Object>() {{
            put("sub", "f99530d4-8317-4900-bd02-0127bb8c44de");
            put("name", "张三");
            put("preferred_username", "zhangsan");
            put("email", "zhangsan@example.com");
            put("phone", "13211111111");
        }};

        String token = jwtAuth.newLoginURLWithClaims(claims, Constants.APP_TENCENT_MEETING);
        System.out.println(token);
    }
}
