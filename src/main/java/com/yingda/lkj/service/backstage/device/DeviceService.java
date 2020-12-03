package com.yingda.lkj.service.backstage.device;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.pojo.device.SemaphoreFromExcel;
import com.yingda.lkj.beans.system.Page;
import com.yingda.lkj.beans.system.Pair;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/1/3
 */
public interface DeviceService {

    /**
     * 保存扩展字段数据
     * @param device 对应设备
     * @param parameterMap 扩展字段Map key:对应扩展字段id(DeviceExtendField.id), value: 对应的字段的值
     */
    void save(Device device, Map<String, String> parameterMap) throws Exception;

    List<Device> importSemaphores(List<SemaphoreFromExcel> semaphoresFromExcel) throws Exception;

    List<Device> getDevicesByIds(List<String> deviceIds);

    List<Device> getBySectionId(String sectionId);

    Device getById(String deviceId);

    void deleteByIds(List<String> ids);

    Device getByLineStationAndCode(String railwayLineName, String stationName, String code);

    Device getByStationIdAndCode(String stationId, String code);

    List<Device> getByStationId(String stationId);

    List<Device> getByDeviceType(String deviceTypeId);

    List<Device> getByDeviceSubType(String deviceSubTypeId);
}
