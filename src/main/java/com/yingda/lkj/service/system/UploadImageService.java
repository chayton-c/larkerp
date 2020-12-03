package com.yingda.lkj.service.system;

import com.yingda.lkj.beans.entity.system.UploadImage;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/6/29
 */
public interface UploadImageService {
    UploadImage getById(String id);

    /**
     * @return key: uploadImage.id value:uploadImage
     */
    Map<String, UploadImage> getByIds(List<String> ids);
}
