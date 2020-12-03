package com.yingda.lkj.service.app.lkj;

import com.yingda.lkj.beans.pojo.app.AppLkjDataLineReceive;
import com.yingda.lkj.beans.pojo.app.AppLkjFreeMeasurementReceivce;
import com.yingda.lkj.beans.pojo.app.AppLkjTaskReceive;

import java.util.List;

/**
 * 从app收lkj数据
 *
 * @author hood  2020/4/26
 */
public interface AppLkjDataLineReceiveService {
    /**
     *  1.首先按照app上传的值填写测量距离 2.生成路径信息
     */
    void saveLkjDataLines(List<AppLkjDataLineReceive> appLkjDataLines);
    /**
     * 1.生成自由测量数据 2.保存路径
     */
    void saveFreeLkjDataLines(List<AppLkjFreeMeasurementReceivce> appLkjFreeMeasurementReceivces, String executeUserIds);
}
