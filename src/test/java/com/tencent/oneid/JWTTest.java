package com.tencent.oneid;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: marcdai
 * @date: 2024/3/28
 */
public class JWTTest {
    String testLoginBaseURL = "https://oauth2.eid-6.account.tencentcs.com/v1/sso/jwtp/1102878596482998272/1151383032381308928/kit/{app_type}";
    String testIssuer       = "https://www.example.com";
    String testPrivKey      = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDPwlvSsvsxHHKkRFeMvrBPvfGio2TLEHBCsoZ34KBmpjrJHLpcvVQ7K3SX3bRfplWH2qPs5EI9zt+LQ6Jlr1rMj7Nh/ZlX698rShdBtsfLX5rlFyFlJrQPOLnX1d9lD1i2FWFrCYe/CwHqx8+Y25KIgci1lyU7CgQXD944+Hkqv1pmYrqZJvl12fTR3gx2fiC/iAsFEBTpdSWavleE6i3vKPdfsp+Ojs9bHcv5btkPIBLGVMV2oRGjHxZdDwRQSaHo9DwnSSv6p+S+xcdALHRLMUNonQ1R9hDFRLRt7/G8fB+4+OrA4I5hmYZWOV9zi8CJ/S57miPLLHcrMEa8fWSnAgMBAAECggEACxTl4EY1tHfnptq9BL/Yba3G/r19DyvFoSPJR7ROj0sckETyV9ICyn6AjefVytL3dZ30PRrWbFo60usnoAmLa/qE6fF58BZKZWe399mvrH8L/F47JMcSDEx39TWY4INstZb3BvDk3GF87QX9YmeL2Ft71jEasPHRfV1rpVmeNOkUEaV1hLYsf3l9AZ7Im8hN+2Aarp0m7oMdOY3QVZ5bQ4qlbYsjPT3aCaZIpHoUCmUrKuyqNQDpXqXYZ6imBgaU9SzYHROJ+etAxyef8d/DbshrZ77OkI+xO1Nq8OClqfOBLeKnT4tr1S0t31mBWE1fFf1590UKmovZ2mm+zrXD8QKBgQDpU0g5fUcWBdsOeL+I6VZwB+iQcRIid0XiKI1bB4mdS+cA3JNFszy5y8jHY+2amVZ7Wvorl3ZICaGMLJncdChz4e2yJ5icAmLpJ7RVNnFm5oqM+EcXW5mLS99d7GlauY0ORUdDytdfv0aWqFwkltMNV7Z1VY4C+O0X2wsPwIK2DwKBgQDj8wmTSF/P4q4vhl5VFHK5HR5KaTxZ09myd82Xl1MTADjV3E3MBkDUWGhnRYFmwLCmnuXuMBTdA6nLHEpDnWW6Q1Xtbmt5k1x9D8B9nwbA2Tmz9hGvN1l8MlYPt2Hu+E17Je6kMCdy5Iz1QUevXc3cR0DLZwFGRhgXAyIS8cg/6QKBgQCuswrK8MA+/xdrmIFg08VCkMlTDTZU1BVhJpfgZp5lRiWqgX1LnM6FFs44bNvE+7bDGfVimj+X5I4u1F5HsDlxuuIsmHUtqqPAi1f8zYzPTSLENkmUdaNbpu2R96dSpMe2vayEV+Y27JK/z0NeqgdQYDJfXDW+h/+N8xYvLycvhQKBgDezFW3ly3OywjlergJAIuBU2yf3mwWgHJvdZmFaWrRT449ua5wlEwZQLALAGySOhRvRzAFtwktXL9Avs33eIhNnjMGdr6lfdsQgazrG9xF8gvsUb7HO5pDQg/MHLmkER3qGBFAebCVI76CmOOwDEeB3kL+jBc60JgLJgzP53KKxAoGBANKZ9xIWiSyRICUIHwpWClizXj9dyXaHOl6INqd/Jj+1dqdizI7YoVufm6vDP0vKf467HKLwLm5mDlZr3j+j/Y/WkbZqluT8onPx4F7m5f8dJUu/OJtGBc1+OnfzyFt5xSAD0Q6NDAxDdKuKCV36znRdNbZu/WiICncDQIjaNCeQ\n" +
            "-----END PRIVATE KEY-----";

    @Test
    public void newConfig() throws Exception {
        Config config = JWT.newConfig(testLoginBaseURL, testIssuer, testPrivKey);

        //设置id_token有效期
        config.setTokenLifetime(600);//10min
    }

    @Test
    public void newTokenWithUserInfo() throws Exception {
        Config config = JWT.newConfig(testLoginBaseURL, testIssuer, testPrivKey);

        UserInfo userInfo = new UserInfo().setId("f99530d4-8317-4900-bd02-0127bb8c44de")
                .setName("张三")
                .setPreferredUsername("zhangsan")
                .setEmail("zhangsan@example.com")
                .setMobile("+86 13211111111")
                .setExtension(Collections.singletonMap("picture", "https://www.example.com/avatar1.png"));

        String token = JWT.newToken(config, userInfo);
        System.out.println(token);
    }

    @Test
    public void newTokenWithClaims() throws Exception {
        Config config = JWT.newConfig(testLoginBaseURL, testIssuer, testPrivKey);

        Map<String, Object> claims = new HashMap<String, Object>(){{
            put("id", "f99530d4-8317-4900-bd02-0127bb8c44de");
            put("name", "张三");
            put("gonghao", "123456");
            put("email", "zhangsan@example.com");
            put("phone", "13211111111");
        }};

        String token = JWT.newTokenWithClaims(config, claims);
        System.out.println(token);
    }

    @Test
    public void newLoginURL() throws Exception {
        Config config = JWT.newConfig(testLoginBaseURL, testIssuer, testPrivKey);

        UserInfo userInfo = new UserInfo().setId("f99530d4-8317-4900-bd02-0127bb8c44de")
                .setName("张三")
                .setPreferredUsername("zhangsan")
                .setEmail("zhangsan@example.com")
                .setMobile("+86 13211111111")
                .setExtension(Collections.singletonMap("picture", "https://www.example.com/avatar1.png"));

        String url = JWT.newLoginURL(config, userInfo, Constants.APP_TENCENT_MEETING);
        System.out.println(url);
    }

    @Test
    public void newLoginURLWithClaims() throws Exception {
        Config config = JWT.newConfig(testLoginBaseURL, testIssuer, testPrivKey);

        Map<String, Object> claims = new HashMap<String, Object>(){{
            put("id", "f99530d4-8317-4900-bd02-0127bb8c44de");
            put("name", "张三");
            put("gonghao", "123456");
            put("email", "zhangsan@example.com");
            put("phone", "13211111111");
        }};
        String token = JWT.newLoginURLWithClaims(config, claims, Constants.APP_TENCENT_MEETING);
        System.out.println(token);
    }
}
