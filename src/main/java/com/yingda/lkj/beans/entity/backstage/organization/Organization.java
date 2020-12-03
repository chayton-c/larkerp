package com.yingda.lkj.beans.entity.backstage.organization;

import com.yingda.lkj.beans.Constant;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author hood  2019/12/26
 */
@Entity
@Table(name = "organization")
public class Organization {

    // 根目录id
    public static final String ROOT_ID = "0";

    // level字段
    public static final byte BUREAU = 1; // 局
    public static final byte SECTION = 2; // 段
    public static final byte WORKSHOP = 3; // 车间
    public static final byte WORK_AREA = 4; // 工区

    private String id;
    private String parentId;
    private String name;
    private String shortName;
    private String description;
    private String remark;
    private byte level;
    private String code;
    private Timestamp addTime;
    private Timestamp updateTime;
    private int seq;
    private byte hide;

    // page field
    private List<Organization> organizationList = new ArrayList<>(); // 下级
    private boolean hasSlave; // 是否有下级
    private boolean checked; // 是否被勾选

    public Organization() {
    }

    public Organization(Organization pageOrganization, Organization parentOrganization) {
        this.id = UUID.randomUUID().toString();

        this.parentId = pageOrganization.getParentId();
        this.name = pageOrganization.getName();
        this.shortName = this.name; // 简写名逻辑暂时没有
        this.description = pageOrganization.getDescription();
        this.remark = pageOrganization.getRemark();
        this.seq = pageOrganization.getSeq();
        this.level = (byte) (parentOrganization.getLevel() + 1);
        this.code = "fff";
        this.addTime = new Timestamp(System.currentTimeMillis());
        this.hide = Constant.FALSE;
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
    @Column(name = "parent_id", nullable = false, length = 36)
    public String   getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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
    @Column(name = "description", nullable = true, length = 255)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "remark", nullable = true, length = 255)
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Basic
    @Column(name = "level", nullable = false)
    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    @Basic
    @Column(name = "code", nullable = false, length = 255)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
    @Column(name = "seq", nullable = false)
    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    @Transient
    public List<Organization> getOrganizationList() {
        return organizationList;
    }

    public void setOrganizationList(List<Organization> organizationList) {
        this.organizationList = organizationList;
    }

    @Transient
    public boolean isHasSlave() {
        return hasSlave;
    }

    public void setHasSlave(boolean hasSlave) {
        this.hasSlave = hasSlave;
    }

    @Transient
    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization that = (Organization) o;
        return level == that.level &&
                Objects.equals(id, that.id) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(code, that.code) &&
                Objects.equals(addTime, that.addTime) &&
                Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentId, name, description, remark, level, code, addTime, updateTime);
    }

    @Basic
    @Column(name = "short_name", nullable = true, length = 255)
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Basic
    @Column(name = "hide", nullable = false)
    public byte getHide() {
        return hide;
    }

    public void setHide(byte hide) {
        this.hide = hide;
    }
}
