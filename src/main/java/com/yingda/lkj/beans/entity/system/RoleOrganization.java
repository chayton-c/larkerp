package com.yingda.lkj.beans.entity.system;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

/**
 * @author hood  2020/11/26
 */
@Entity
@Table(name = "role_organization", schema = "power_plant_etms")
public class RoleOrganization {
    private String id;
    private String roleId;
    private String organizationId;
    private Timestamp addTime;

    public RoleOrganization() {
    }

    public RoleOrganization(String roleId, String organizationId) {
        this.id = UUID.randomUUID().toString();
        this.roleId = roleId;
        this.organizationId = organizationId;
        this.addTime = new Timestamp(System.currentTimeMillis());
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
    @Column(name = "role_id", nullable = false, length = 36)
    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    @Basic
    @Column(name = "organization_id", nullable = false, length = 36)
    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    @Basic
    @Column(name = "add_time", nullable = true)
    public Timestamp getAddTime() {
        return addTime;
    }

    public void setAddTime(Timestamp addTime) {
        this.addTime = addTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleOrganization that = (RoleOrganization) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(organizationId, that.organizationId) &&
                Objects.equals(roleId, that.roleId) &&
                Objects.equals(addTime, that.addTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, organizationId, roleId, addTime);
    }
}
