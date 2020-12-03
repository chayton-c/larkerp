package com.yingda.lkj.beans.pojo.system;

import java.util.List;

/**
 * @author hood  2020/11/11
 */
public class NgAppData {
    private NgAppDetail app;
    private NgUser user;
    private List<NgMenu> menu;

    public NgAppData(NgAppDetail app, NgUser user, List<NgMenu> menu) {
        this.app = app;
        this.user = user;
        this.menu = menu;
    }

    public NgAppDetail getApp() {
        return app;
    }

    public void setApp(NgAppDetail app) {
        this.app = app;
    }

    public NgUser getUser() {
        return user;
    }

    public void setUser(NgUser user) {
        this.user = user;
    }

    public List<NgMenu> getMenu() {
        return menu;
    }

    public void setMenu(List<NgMenu> menu) {
        this.menu = menu;
    }
}
