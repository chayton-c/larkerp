package com.yingda.lkj.beans.entity.system;

import com.yingda.lkj.utils.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author hood  2019/12/18
 */
@Entity
@Table(name = "user")
public class User implements Serializable {

    /**
     * 必填项
     */
    public static final Map<String, String> REQUIRED_MAP = Map.of(
            "userName", "用户名", "displayName", "显示姓名", "roleId", "用户角色"
    );

    // 用户被封禁 0: 没被禁，1：被封禁
    public static final Byte NOT_BANNED = 0;
    public static final Byte BANNED = 1;

    public static final Byte NOT_DELETED = 0;
    public static final Byte DELETED = 1;

    private String id;
    private String userName;
    private String displayName;
    private String password;
    private String roleId; // 角色id Role.id
    private byte banned;
    private Timestamp addTime;
    private Timestamp updateTime;
    private byte deleted;
    private String bureauId;
    private String sectionId;
    private String workshopId;
    private String workAreaId;
    private Timestamp loginTime;

    // page fields
    private String bureauName;
    private String sectionName;
    private String workshopName;
    private String workAreaName;
    private String roleName;
    private String token;
    private String akagi;

    public User() {
    }

    public User(User user) {
        Timestamp current = new Timestamp(System.currentTimeMillis());

        this.id = UUID.randomUUID().toString();
        this.userName = user.getUserName();
        this.password = "123456";
        this.banned = NOT_BANNED;
        this.addTime = current;
        this.updateTime = current;
        this.deleted = NOT_DELETED;
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
    @Column(name = "user_name", nullable = false, length = 30, unique = true)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "password", nullable = false, length = 255)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
    @Column(name = "banned", nullable = false)
    public byte getBanned() {
        return banned;
    }

    public void setBanned(byte banned) {
        this.banned = banned;
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
    @Column(name = "deleted", nullable = false)
    public byte getDeleted() {
        return deleted;
    }

    public void setDeleted(byte deleted) {
        this.deleted = deleted;
    }

    @Basic
    @Column(name = "display_name", nullable = false, length = 255)
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Basic
    @Column(name = "bureau_id", length = 36)
    public String getBureauId() {
        return bureauId;
    }

    public void setBureauId(String bureauId) {
        this.bureauId = bureauId;
    }

    @Basic
    @Column(name = "section_id", length = 36)
    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    @Basic
    @Column(name = "workshop_id", length = 36)
    public String getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(String workshopId) {
        this.workshopId = workshopId;
    }

    @Basic
    @Column(name = "work_area_id", length = 36)
    public String getWorkAreaId() {
        return workAreaId;
    }

    public void setWorkAreaId(String workAreaId) {
        this.workAreaId = workAreaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return banned == user.banned &&
                deleted == user.deleted &&
                Objects.equals(id, user.id) &&
                Objects.equals(userName, user.userName) &&
                Objects.equals(displayName, user.displayName) &&
                Objects.equals(password, user.password) &&
                Objects.equals(roleId, user.roleId) &&
                Objects.equals(addTime, user.addTime) &&
                Objects.equals(updateTime, user.updateTime) &&
                Objects.equals(bureauId, user.bureauId) &&
                Objects.equals(sectionId, user.sectionId) &&
                Objects.equals(workshopId, user.workshopId) &&
                Objects.equals(workAreaId, user.workAreaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, addTime, banned, deleted, password, roleId, bureauId, sectionId, workshopId, workAreaId, updateTime,
                displayName, userName);
    }

    @Transient
    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    @Transient
    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    @Transient
    public String getWorkAreaName() {
        return workAreaName;
    }

    public void setWorkAreaName(String workAreaName) {
        this.workAreaName = workAreaName;
    }

    @Basic
    @Column(name = "login_time", nullable = true)
    public Timestamp getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Timestamp loginTime) {
        this.loginTime = loginTime;
    }

    @Transient
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Transient
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Transient
    public String getBureauName() {
        return bureauName;
    }

    public void setBureauName(String bureauName) {
        this.bureauName = bureauName;
    }


}
