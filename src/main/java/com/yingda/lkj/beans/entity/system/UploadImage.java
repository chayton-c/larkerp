package com.yingda.lkj.beans.entity.system;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * 上传的图片
 *
 * @author hood  2020/6/28
 */
@Entity
@Table(name = "upload_image", schema = "illustrious", catalog = "")
public class UploadImage {
    private String id;
    private String url;
    private Timestamp addTime;

    @Id
    @Column(name = "id", nullable = false, length = 36)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        UploadImage that = (UploadImage) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(url, that.url) &&
                Objects.equals(addTime, that.addTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, addTime);
    }
}
