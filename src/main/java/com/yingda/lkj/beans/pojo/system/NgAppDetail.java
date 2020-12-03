package com.yingda.lkj.beans.pojo.system;

/**
 * @author hood  2020/11/11
 */
public class NgAppDetail {
    private String name;
    private String description;

    // 先写死,不知道前端哪用到了
    public NgAppDetail() {
        this.name = "Alain";
        this.description = "Ng-zorro admin panel front-end framework";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
