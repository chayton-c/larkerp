package com.yingda.lkj.beans.pojo.system;

import com.yingda.lkj.beans.entity.system.User;

import java.util.List;

/**
 * @author hood  2020/11/11
 */
public class NgUser {
    private String name;
    private String avatar;
    private String email;
    private List<String> menuIds;

    public NgUser() {
        this.name = "";
        this.avatar = "./assets/tmp/img/avatar.jpg";
        this.email = "";
    }

    public NgUser(User user) {
        this.name = user.getDisplayName();
        this.avatar = "./assets/tmp/img/avatar.jpg";
        this.email = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<String> menuIds) {
        this.menuIds = menuIds;
    }
}
