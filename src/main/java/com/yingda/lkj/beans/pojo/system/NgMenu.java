package com.yingda.lkj.beans.pojo.system;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.system.Menu;

import java.util.List;

/**
 * @author hood  2020/11/10
 */
public class NgMenu {
    private String id;
    private String pid;
    private String text;
    private String i18n;
    private boolean group;
    private boolean hideInBreadcrumb;
    private String icon;
    private String acl;
    private String link;
    private int seq;
    private boolean reuse; // 对应菜单是否路由复用，false:不复用
    private List<NgMenu> children;

    public NgMenu(Menu menu) {
        this.id = menu.getId();
        this.pid = menu.getPid();
        this.text = menu.getName();
        this.i18n = menu.getId();
        this.group = menu.getLevel() == Menu.PRIMARY_MENU;
        this.hideInBreadcrumb = menu.getLevel() == Menu.PRIMARY_MENU;
        this.link = menu.getUrl();
        this.acl = menu.getId();
        this.seq = menu.getSeq();
        this.reuse = menu.getReuse() == Constant.TRUE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getI18n() {
        return i18n;
    }

    public void setI18n(String i18n) {
        this.i18n = i18n;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public boolean isHideInBreadcrumb() {
        return hideInBreadcrumb;
    }

    public void setHideInBreadcrumb(boolean hideInBreadcrumb) {
        this.hideInBreadcrumb = hideInBreadcrumb;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAcl() {
        return acl;
    }

    public void setAcl(String acl) {
        this.acl = acl;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public List<NgMenu> getChildren() {
        return children;
    }

    public void setChildren(List<NgMenu> children) {
        this.children = children;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isReuse() {
        return reuse;
    }

    public void setReuse(boolean reuse) {
        this.reuse = reuse;
    }
}
