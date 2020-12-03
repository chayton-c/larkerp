package com.yingda.lkj.service.impl.backstage.device;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendField;
import com.yingda.lkj.beans.entity.backstage.device.DeviceExtendValues;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.pojo.device.SemaphoreFromExcel;
import com.yingda.lkj.beans.system.Page;
import com.yingda.lkj.beans.system.Pair;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.device.DeviceExtendFieldService;
import com.yingda.lkj.service.backstage.device.DeviceExtendValuesService;
import com.yingda.lkj.service.backstage.device.DeviceService;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/1/3
 */
@Service("deviceService")
public class DeviceServiceImpl implements DeviceService {
    @Autowired
    private BaseDao<Device> deviceBaseDao;
    @Autowired
    private StationService stationService;
    @Autowired
    private DeviceExtendValuesService deviceExtendValuesService;
    @Autowired
    private DeviceExtendFieldService deviceExtendFieldService;
    @Autowired
    private RailwayLineService railwayLineService;

    @Override
    public void save(Device device, Map<String, String> parameterMap) throws Exception {
        Timestamp current = new Timestamp(System.currentTimeMillis());

        if (StringUtils.isEmpty(device.getId()))
            device.setId(UUID.randomUUID().toString());
        device.setAddTime(current);
        device.setUpdateTime(current);

        deviceBaseDao.saveOrUpdate(device);
        deviceExtendValuesService.saveExtendValues(device, parameterMap);
    }

    @Override
    public List<Device> importSemaphores(List<SemaphoreFromExcel> semaphoresFromExcel) throws Exception {
        semaphoresFromExcel = semaphoresFromExcel.stream().distinct().collect(Collectors.toList());
        Timestamp current = new Timestamp(System.currentTimeMillis());


        // 查询扩展字段，key: 扩展字段名，value：扩展字段id
        List<String> fieldNames = new ArrayList<>(semaphoresFromExcel.get(0).getExtendFieldValues().keySet());
        List<DeviceExtendField> fieldsByNames = deviceExtendFieldService.getFieldsByNames(fieldNames);
        Map<String, String> extendFilesMap = fieldsByNames.stream().collect(Collectors.toMap(DeviceExtendField::getName, DeviceExtendField::getId));

        // 查询线路， key：线路code，value: 线路id
        List<String> railwayLineCodes = semaphoresFromExcel.stream().map(SemaphoreFromExcel::getRailwayLineCode).collect(Collectors.toList());
        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByCodes(railwayLineCodes);
        Map<String, String> railwayLineMap = railwayLines.stream().collect(Collectors.toMap(RailwayLine::getCode, RailwayLine::getId));

        // 查询车站 key: 车站名，value：车站id
        List<String> stationNames = semaphoresFromExcel.stream().map(SemaphoreFromExcel::getStationName).collect(Collectors.toList());
        List<Station> stations = stationService.getStationsByNames(stationNames);
        Map<String, String> stationMap = stations.stream().collect(Collectors.toMap(Station::getName, Station::getId));

        List<Device> devices = new ArrayList<>();
        Map<String, Boolean> distinctMap = new HashMap<>(); // device.同一个站，同一个线路下，code唯一索引去重
        for (SemaphoreFromExcel semaphoreFromExcel : semaphoresFromExcel) {

            String stationId = stationMap.get(semaphoreFromExcel.getStationName());
            String railwayLineId = railwayLineMap.get(semaphoreFromExcel.getRailwayLineCode());
            String code = semaphoreFromExcel.getCode();

            String uniqueCode = stationId + railwayLineId + code;
            if (distinctMap.get(uniqueCode) != null)
                continue;
            distinctMap.put(uniqueCode, true);

            // 生成设备
            Device device = deviceBaseDao.get("from Device where stationId = :stationId and railwayLineId = :railwayLineId and code = :code", Map.of(
                    "stationId", stationId, "railwayLineId", railwayLineId, "code", code));
            device = Optional.ofNullable(device).orElse(new Device());

            if (device.getId() == null)
                device.setId(UUID.randomUUID().toString());

            BeanUtils.copyProperties(semaphoreFromExcel, device);

            device.setStationId(stationId);
            device.setAddTime(current);
            device.setUpdateTime(current);
            devices.add(device);

            // 生成扩展字段
            Map<String, String> extendFieldValues = semaphoreFromExcel.getExtendFieldValues();

            Device finalDevice = device;
            List<DeviceExtendValues> extendValues = extendFieldValues.entrySet().stream().map(x -> new DeviceExtendValues(finalDevice,
                    extendFilesMap.get(x.getKey()),
                    x.getValue())).collect(Collectors.toList());
            device.setExtendValues(extendValues);
        }

        deviceExtendValuesService.saveExtendValues(devices);
//        for (Device device : devices)
//            deviceBaseDao.saveOrUpdate(device);

        return devices;
    }

    @Override
    public List<Device> getDevicesByIds(List<String> deviceIds) {
        String sql = "SELECT\n" +
                "  device.id AS id,\n" +
                "  device.NAME AS name,\n" +
                "  device.CODE AS code,\n" +
                "  line.NAME AS railwayLineName,\n" +
                "  station.NAME AS stationName \n" +
                "FROM\n" +
                "  device\n" +
                "  LEFT JOIN railway_line line ON line.id = device.railway_line_id\n" +
                "  LEFT JOIN station ON station.id = device.station_id \n" +
                "WHERE\n" +
                "  device.id IN :deviceIds\n" +
                "  AND device.hide = :hide\n";

        Map<String, Object> params = Map.of("deviceIds", deviceIds, "hide", Constant.SHOW);

        List<Device> devices = deviceBaseDao.findSQL(sql, params, Device.class, 1, 999999);

        return devices;
    }

    @Override
    public List<Device> getBySectionId(String sectionId) {
        return deviceBaseDao.find("from Device where sectionId = :sectionId", Map.of("sectionId", sectionId));
    }

    @Override
    public Device getById(String deviceId) {
        return deviceBaseDao.get(Device.class, deviceId);
    }

    @Override
    public void deleteByIds(List<String> ids) {
        deviceBaseDao.executeHql(
                "delete from Device where id in :ids",
                Map.of("ids", ids)
        );
    }

    @Override
    public Device getByLineStationAndCode(String railwayLineName, String stationName, String code) {
        String sql = "SELECT\n" +
                "  device.id AS id\n" +
                "FROM\n" +
                "  device\n" +
                "  LEFT JOIN railway_line line ON line.id = device.railway_line_id\n" +
                "  LEFT JOIN station ON station.id = device.station_id \n" +
                "WHERE\n" +
                "  device.code = :code\n" +
                "  AND station.name = :stationName\n" +
                "  AND line.name = :railwayLineName\n";

        Map<String, Object> params = new HashMap<>();
        params.put("railwayLineName", railwayLineName);
        params.put("stationName", stationName);
        params.put("code", code);

        List<Device> devices = deviceBaseDao.findSQL(sql, params, Device.class, 1, 999999);
        if (devices.isEmpty())
            return null;

        return deviceBaseDao.get(Device.class, devices.get(0).getId());
    }

    @Override
    public Device getByStationIdAndCode(String stationId, String code) {
        return deviceBaseDao.get(
                "from Device where stationId = :stationId and code = :code",
                Map.of("stationId", stationId, "code", code)
        );
    }

    @Override
    public List<Device> getByStationId(String stationId) {
        return deviceBaseDao.find(
                "from Device where stationId = :stationId",
                Map.of("stationId", stationId)
        );
    }

    @Override
    public List<Device> getByDeviceType(String deviceTypeId) {
        return deviceBaseDao.find(
                "from Device where deviceTypeId = :deviceTypeId",
                Map.of("deviceTypeId", deviceTypeId)
        );
    }

    @Override
    public List<Device> getByDeviceSubType(String deviceSubTypeId) {
        return deviceBaseDao.find(
                "from Device where deviceSubTypeId = :deviceSubTypeId",
                Map.of("deviceSubTypeId", deviceSubTypeId)
        );
    }
}
