package com.tencent.oneid;

import org.junit.Test;
import org.junit.Assert;

public class UserInfoTest {
    @Test(expected = IllegalArgumentException.class)
    public void emptyID() {
        UserInfo user = new UserInfo(" ", "test");
        Assert.assertNull(user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullID() {
        UserInfo user = new UserInfo(null, "test");
        Assert.assertNull(user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullName() {
        UserInfo user = new UserInfo("id1", null);
        Assert.assertNull(user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyName() {
        UserInfo user = new UserInfo("id1", " ");
        Assert.assertNull(user);
    }

    @Test
    public void trimmedIDAndName() {
        UserInfo user = new UserInfo(" id ", " name ");
        Assert.assertEquals("id", user.getId());
        Assert.assertEquals("name", user.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyUsernameAndMobileAndEmail() {
        UserInfo user = new UserInfo("id", "test");
        user.validate();
    }

    @Test
    public void setUsernameAndMobileAndEmail() {
        UserInfo user = new UserInfo("id", "test").setUsername("username").setMobile("mobile").setEmail("email");
        Assert.assertEquals("username", user.getUsername());
        Assert.assertEquals("mobile", user.getMobile());
        Assert.assertEquals("email", user.getEmail());
    }
}
