package com.yingda.lkj.service.impl.system;

import com.yingda.lkj.beans.entity.system.UploadImage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.system.UploadImageService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/6/29
 */
@Service("uploadImageService")
public class UploadImageServiceImpl implements UploadImageService {

    @Autowired
    private BaseDao<UploadImage> uploadImageBaseDao;

    @Override
    public UploadImage getById(String id) {
        return uploadImageBaseDao.get(UploadImage.class, id);
    }

    @Override
    public Map<String, UploadImage> getByIds(List<String> ids) {
        List<UploadImage> uploadImages = uploadImageBaseDao.find(
                "from UploadImage where id in :ids",
                Map.of("ids", ids)
        );
        return StreamUtil.getMap(uploadImages, UploadImage::getId, x -> x);
    }
}
