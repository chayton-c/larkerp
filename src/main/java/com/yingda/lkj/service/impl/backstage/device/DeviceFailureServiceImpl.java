package com.yingda.lkj.service.impl.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.DeviceFailure;
import com.yingda.lkj.beans.entity.system.UploadImage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceFailureService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hood  2020/8/12
 */
@Service("deviceFailureService")
public class DeviceFailureServiceImpl implements DeviceFailureService {

    @Autowired
    private BaseDao<DeviceFailure> deviceFailureBaseDao;
    @Autowired
    private BaseDao<UploadImage> uploadImageBaseDao;

    @Override
    public DeviceFailure getById(String id) {
        return deviceFailureBaseDao.get(DeviceFailure.class, id);
    }

    @Override
    public List<UploadImage> getDeviceFailureImages(String deviceFailureId) {
        DeviceFailure deviceFailure = getById(deviceFailureId);

        String images = deviceFailure.getImages();
        if (StringUtils.isEmpty(images))
            return new ArrayList<>();

        return Arrays.stream(images.split(",")).map(x -> uploadImageBaseDao.get(UploadImage.class, x)).collect(Collectors.toList());
    }


}
