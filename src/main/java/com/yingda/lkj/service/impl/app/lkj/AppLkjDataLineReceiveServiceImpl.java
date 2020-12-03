package com.yingda.lkj.service.impl.app.lkj;

import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLineLocation;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjFreeMeasurement;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjFreeMeasurementLocation;
import com.yingda.lkj.beans.pojo.app.*;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.app.lkj.AppLkjDataLineReceiveService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/4/26
 */
@Service("appLkjDataLineReceiveService")
public class AppLkjDataLineReceiveServiceImpl implements AppLkjDataLineReceiveService {

    @Autowired
    private BaseDao<LkjDataLine> lkjDataLineBaseDao;
    @Autowired
    private BaseDao<LkjDataLineLocation> lkjDataLineLocationBaseDao;
    @Autowired
    private BaseDao<LkjFreeMeasurement> lkjFreeMeasurementBaseDao;
    @Autowired
    private BaseDao<LkjFreeMeasurementLocation> lkjFreeMeasurementLocationBaseDao;

    @Override
    public void saveLkjDataLines(List<AppLkjDataLineReceive> appLkjDataLines) {
        Timestamp current = new Timestamp(System.currentTimeMillis());

        // 1.首先按照app上传的值填写测量距离
        List<String> lkjDataLineIds = StreamUtil.getList(appLkjDataLines, AppLkjDataLineReceive::getId);
        // key:lkjDataLine.id vulue:app上传的进路距离
        Map<String, Double> lkjDistanceMap = StreamUtil.getMap(appLkjDataLines, AppLkjDataLineReceive::getId, AppLkjDataLineReceive::getDistance);

        List<LkjDataLine> lkjDataLines = lkjDataLineBaseDao.find(
                "from LkjDataLine where id in :ids",
                Map.of("ids", lkjDataLineIds)
        );

        for (LkjDataLine lkjDataLine : lkjDataLines) {
            String lkjDataLineId = lkjDataLine.getId();
            Double distance = lkjDistanceMap.get(lkjDataLineId);

            lkjDataLine.setUpdateTime(current);
            lkjDataLine.setDistance(distance);
        }

        lkjDataLineBaseDao.bulkInsert(lkjDataLines);

        // 2.生成路径信息
        // 删除旧的路径信息
        lkjDataLineLocationBaseDao.executeHql(
                "delete from LkjDataLineLocation where lkjDataLineId in :lkjDataLineIds",
                Map.of("lkjDataLineIds", lkjDataLineIds)
        );

        // 生成新的路径信息
        List<LkjDataLineLocation> lkjDataLineLocations = new ArrayList<>();
        for (AppLkjDataLineReceive appLkjDataLine : appLkjDataLines) {
            String lkjDataLineId = appLkjDataLine.getId();
            List<AppLkjNodeReceive> nodes = appLkjDataLine.getDeviceList();
            for (AppLkjNodeReceive node : nodes) {
                AppLocationReceive locationEnd = node.getLocationEnd();
                if (locationEnd == null) // 如果不记录路径，locationEnd为null，不生成
                    continue;

                lkjDataLineLocations.add(new LkjDataLineLocation(node, lkjDataLineId));
            }
        }

        lkjDataLineLocationBaseDao.bulkInsert(lkjDataLineLocations);
    }

    @Override
    public void saveFreeLkjDataLines(List<AppLkjFreeMeasurementReceivce> appLkjFreeMeasurementReceivces, String executeUserIds) {
        // 1.生成自由测量数据
        List<LkjFreeMeasurement> lkjFreeMeasurements = new ArrayList<>();
        for (AppLkjFreeMeasurementReceivce appLkjFreeMeasurementReceivce : appLkjFreeMeasurementReceivces) {
            List<AppLkjFreeNodeReceive> nodes = appLkjFreeMeasurementReceivce.getDeviceList();
            nodes.sort(Comparator.comparingInt(o -> (int) o.getMeasure_time()));

            String pointNames = nodes.get(0).getLineName() + " -> " + nodes.get(nodes.size() - 1).getLineName();

            LkjFreeMeasurement lkjFreeMeasurement = new LkjFreeMeasurement(appLkjFreeMeasurementReceivce, executeUserIds, pointNames);
            lkjFreeMeasurement.setNodes(nodes);
            lkjFreeMeasurements.add(lkjFreeMeasurement);
        }

        lkjFreeMeasurementBaseDao.bulkInsert(lkjFreeMeasurements);
        // end of 1

        // 2.保存路径
        List<LkjFreeMeasurementLocation> lkjFreeMeasurementLocations = new ArrayList<>();
        for (LkjFreeMeasurement lkjFreeMeasurement : lkjFreeMeasurements) {
            String lkjFreeMeasurementId = lkjFreeMeasurement.getId();

            List<AppLkjFreeNodeReceive> nodes = lkjFreeMeasurement.getNodes();
            for (AppLkjFreeNodeReceive node : nodes) {
                AppLocationReceive locationEnd = node.getLocationEnd();
                if (locationEnd == null)
                    continue;

                lkjFreeMeasurementLocations.add(new LkjFreeMeasurementLocation(node, lkjFreeMeasurementId));
            }
        }
        lkjFreeMeasurementLocationBaseDao.bulkInsert(lkjFreeMeasurementLocations);
    }
}
