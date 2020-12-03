package com.yingda.lkj.beans.entity.system;

import com.yingda.lkj.beans.Constant;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
 * @author hood  2019/12/18
 */
@Entity
@Table(name = "menu")
public class Menu implements Serializable {
    // type 字段
    public static final byte VUE = 1;
    public static final byte THYMELEAF = 0;

    // 根目录id
    public static final String ROOT_ID = "0";
    // level 主次级菜单
    public static final int PRIMARY_MENU = 1;
    public static final int SECONDARY_MENU = 2;

    private String id;
    private String pid; // 上级菜单id
    private String url; // 路径
    private String name; // 菜单名称
    private Timestamp addTime;
    private Timestamp updateTime;
    private Byte hide; // 是否隐藏，预留字段不用管
    private String description;
    private int seq; // 排序
    private int level; // 层级
    private String icon; // 图标，thymeleaf有bug,我就没放
    private String component;
    private String path;
    private byte type;
    private byte reuse; // 该菜单在路由中是否复用 Constant.FALSE:不复用
    // page field
    private String key;
    private boolean hasAuth = false;
    private List<Menu> children;

    public Menu() {
    }

    public Menu(String id, String pid, String url, String name, int seq, int level) {
        this.id = id;
        this.pid = pid;
        this.url = url;
        this.name = name;
        this.addTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = addTime;
        this.hide = 0;
        this.description = name;
        this.seq = seq;
        this.level = level;
        this.reuse = Constant.FALSE;
    }

    @Id
    @Column(name = "id", nullable = false, length = 36)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "pid", nullable = false, length = 36)
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Basic
    @Column(name = "url", nullable = false, length = 255)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "add_time", nullable = true)
    public Timestamp getAddTime() {
        return addTime;
    }

    public void setAddTime(Timestamp addTime) {
        this.addTime = addTime;
    }

    @Basic
    @Column(name = "update_time", nullable = true)
    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Basic
    @Column(name = "hide", nullable = false)
    public Byte getHide() {
        return hide;
    }

    public void setHide(byte hide) {
        this.hide = hide;
    }

    public void setHide(Byte hide) {
        this.hide = hide;
    }

    @Transient
    public boolean isHasAuth() {
        return hasAuth;
    }

    public void setHasAuth(boolean hasAuth) {
        this.hasAuth = hasAuth;
    }

    @Transient
    public List<Menu> getChildren() {
        return children;
    }

    public void setChildren(List<Menu> children) {
        this.children = children;
    }

    @Basic
    @Column(name = "description", nullable = false, length = 255)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "seq", nullable = false)
    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    @Basic
    @Column(name = "level", nullable = false)
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Basic
    @Column(name = "reuse", nullable = false)
    public byte getReuse() {
        return reuse;
    }

    public void setReuse(byte reuse) {
        this.reuse = reuse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return seq == menu.seq &&
                level == menu.level &&
                Objects.equals(id, menu.id) &&
                Objects.equals(pid, menu.pid) &&
                Objects.equals(url, menu.url) &&
                Objects.equals(name, menu.name) &&
                Objects.equals(addTime, menu.addTime) &&
                Objects.equals(updateTime, menu.updateTime) &&
                Objects.equals(hide, menu.hide) &&
                Objects.equals(description, menu.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, pid, url, description, hide, seq, level, addTime, updateTime);
    }

    @Basic
    @Column(name = "icon", nullable = true, length = 255)
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Basic
    @Column(name = "component", nullable = true, length = 255)
    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    @Basic
    @Column(name = "path", nullable = true, length = 255)
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Basic
    @Column(name = "type", nullable = false)
    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    @Transient
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
