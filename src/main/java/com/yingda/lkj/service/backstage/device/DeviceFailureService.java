package com.yingda.lkj.service.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.DeviceFailure;
import com.yingda.lkj.beans.entity.system.UploadImage;

import java.util.List;

/**
 * @author hood  2020/8/12
 */
public interface DeviceFailureService {

    DeviceFailure getById(String id);

    List<UploadImage> getDeviceFailureImages(String deviceFailureId);

}
