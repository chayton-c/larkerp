package com.yingda.lkj.beans.entity.system;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

/**
 * @author hood  2020/6/1
 */
@Entity
@Table(name = "hql_version", schema = "illustrious", catalog = "")
public class HqlVersion {
    private String id;
    private String hqlVersion;
    private String sqlVersion;
    private Timestamp updateTime;

    public HqlVersion() {
    }

    public HqlVersion(String id) {
        this.id = UUID.randomUUID().toString();
        this.hqlVersion = "1.0";
        this.sqlVersion = "1.0";
        this.updateTime = new Timestamp(System.currentTimeMillis());
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
    @Column(name = "hql_version", nullable = false, length = 255)
    public String getHqlVersion() {
        return hqlVersion;
    }

    public void setHqlVersion(String hqlVersion) {
        this.hqlVersion = hqlVersion;
    }

    @Basic
    @Column(name = "sql_version", nullable = false, length = 255)
    public String getSqlVersion() {
        return sqlVersion;
    }

    public void setSqlVersion(String sqlVersion) {
        this.sqlVersion = sqlVersion;
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
        HqlVersion that = (HqlVersion) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(hqlVersion, that.hqlVersion) &&
                Objects.equals(sqlVersion, that.sqlVersion) &&
                Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, hqlVersion, sqlVersion, updateTime);
    }
}
