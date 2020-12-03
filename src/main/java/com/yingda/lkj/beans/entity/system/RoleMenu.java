package com.yingda.lkj.beans.entity.system;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

/**
 * @author hood  2019/12/18
 */
@Entity
@Table(name = "role_menu")
public class RoleMenu implements Serializable {
    private String id;
    private Timestamp addTime;
    private Timestamp updateTime;
    private String roleId;
    private String menuId;

    public RoleMenu() {
    }

    public RoleMenu(String roleId, String menuId) {
        Timestamp current = new Timestamp(System.currentTimeMillis());

        this.id = UUID.randomUUID().toString();
        this.addTime = current;
        this.updateTime = current;
        this.roleId = roleId;
        this.menuId = menuId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleMenu roleMenu = (RoleMenu) o;
        return Objects.equals(id, roleMenu.id) &&
                Objects.equals(addTime, roleMenu.addTime) &&
                Objects.equals(updateTime, roleMenu.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, addTime, updateTime);
    }

    @Basic
    @Column(name = "role_id", nullable = false, length = 36)
    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    @Basic
    @Column(name = "menu_id", nullable = false, length = 36)
    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }
}
