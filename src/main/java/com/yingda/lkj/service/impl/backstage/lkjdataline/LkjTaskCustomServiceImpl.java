package com.yingda.lkj.service.impl.backstage.lkjdataline;

import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.lkjdataline.LkjTaskCustomService;
import com.yingda.lkj.service.backstage.lkjtask.LkjTaskService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/3/26
 */
@Service("lkjTaskCustomService")
public class LkjTaskCustomServiceImpl implements LkjTaskCustomService {

    @Autowired
    private BaseDao<LkjDataLine> lkjDataLineBaseDao;
    @Autowired
    private LkjTaskService lkjTaskService;

    @Override
    public void appendDevice(String baseLkjDataLineId, String nextLkjDataLineId, String lkjTaskId, String deviceId) {
        LkjDataLine baseLkjDataLine = lkjDataLineBaseDao.get(LkjDataLine.class, baseLkjDataLineId);
        String lkjGroupId = baseLkjDataLine.getLkjGroupId();

        // 1.生成待修改的lkjDataLines
        List<LkjDataLine> lkjDataLines = copyGroups(lkjTaskId, lkjGroupId);

        // 2.额外生成两个lkjDataLine
        // 生成baseLkjDataLine的rightDevice到'插入设备'间的lkjDataLine
        LkjDataLine leftLkj = new LkjDataLine(baseLkjDataLine, lkjTaskId);
        leftLkj.setLeftDeviceId(baseLkjDataLine.getRightDeviceId());
        leftLkj.setRightDeviceId(deviceId);
        leftLkj.setDistance(0);
        leftLkj.setReadonly(LkjDataLine.EDITABLE);
        leftLkj.setLkjGroupId(baseLkjDataLine.getLkjGroupId());
        leftLkj.setSeq(leftLkj.getSeq() + 1);
        lkjDataLines.stream().filter(x -> x.getSeq() >= leftLkj.getSeq()).forEach(x -> x.setSeq(x.getSeq() + 1)); // 后面的排序依次 + 1
        lkjDataLines.add(leftLkj);

        // 特例，如果没有nextLkjDataLineId，表明是在最后一个节点插入，这时可以直接保存提交
        if (StringUtils.isEmpty(nextLkjDataLineId)) {
            lkjDataLineBaseDao.bulkInsert(lkjDataLines);
            return;
        }

        LkjDataLine nextLkjDataLine = lkjDataLineBaseDao.get(LkjDataLine.class, nextLkjDataLineId); // 我知道一起查快，但是这样好看，慢不了多少
        // 生成 '插入设备'到nextLkjDataLine的rightDevice间的lkjDataLine
        LkjDataLine rightLkj = new LkjDataLine(nextLkjDataLine, lkjTaskId);
        rightLkj.setLeftDeviceId(deviceId);
        rightLkj.setRightDeviceId(nextLkjDataLine.getRightDeviceId());
        rightLkj.setDistance(0);
        rightLkj.setReadonly(LkjDataLine.EDITABLE);
        rightLkj.setLkjGroupId(nextLkjDataLine.getLkjGroupId());
        rightLkj.setSeq(rightLkj.getSeq() + 1);
        lkjDataLines.stream().filter(x -> x.getSeq() >= rightLkj.getSeq()).forEach(x -> x.setSeq(x.getSeq() + 1)); // 后面的排序依次 + 1
        lkjDataLines.add(rightLkj);

        // 3.去掉nextLkjDataLineId对应的新生成的那条lkjDataLine
        LkjDataLine needToRemove = lkjDataLines
                .stream()
                .filter(lkjDataLine ->
                        lkjDataLine.getLeftDeviceId().equals(nextLkjDataLine.getLeftDeviceId())
                                && lkjDataLine.getRightDeviceId().equals(nextLkjDataLine.getRightDeviceId())
                )
                .reduce(null, (x, y) -> y);

        lkjDataLines.remove(needToRemove);
        lkjDataLineBaseDao.delete(needToRemove);

        lkjDataLineBaseDao.bulkInsert(lkjDataLines);
    }

    @Override
    public void prependDevice(String baseLkjDataLineId, String previousDataLineId, String lkjTaskId, String deviceId) {
        LkjDataLine baseLkjDataLine = lkjDataLineBaseDao.get(LkjDataLine.class, baseLkjDataLineId);
        String lkjGroupId = baseLkjDataLine.getLkjGroupId();

        // 1.生成待修改的lkjDataLines
        List<LkjDataLine> lkjDataLines = copyGroups(lkjTaskId, lkjGroupId);

        // 2.额外生成两个lkjDataLine
        // 生成'插入设备'到baseLkjDataLine的leftDevice到间的lkjDataLine
        LkjDataLine leftLkj = new LkjDataLine(baseLkjDataLine, lkjTaskId);
        leftLkj.setLeftDeviceId(deviceId);
        leftLkj.setRightDeviceId(baseLkjDataLine.getLeftDeviceId());
        leftLkj.setDistance(0);
        leftLkj.setReadonly(LkjDataLine.EDITABLE);
        leftLkj.setLkjGroupId(baseLkjDataLine.getLkjGroupId());
        leftLkj.setSeq(leftLkj.getSeq() - 1);
        lkjDataLines.stream().filter(x -> x.getSeq() <= leftLkj.getSeq()).forEach(x -> x.setSeq(x.getSeq() - 1)); // 后面的排序依次 + 1
        lkjDataLines.add(leftLkj);

        // 特例，如果没有previousDataLineId，表明是在第一个节点插入，这时可以直接保存提交
        if (StringUtils.isEmpty(previousDataLineId)) {
            lkjDataLineBaseDao.bulkInsert(lkjDataLines);
            return;
        }

        LkjDataLine previousLkjDataLine = lkjDataLineBaseDao.get(LkjDataLine.class, previousDataLineId); // 我知道一起查快，但是这样好看，慢不了多少
        // 生成 '插入设备'到nextLkjDataLine的rightDevice间的lkjDataLine
        LkjDataLine rightLkj = new LkjDataLine(previousLkjDataLine, lkjTaskId);
        rightLkj.setLeftDeviceId(previousLkjDataLine.getLeftDeviceId());
        rightLkj.setRightDeviceId(deviceId);
        rightLkj.setDistance(0);
        rightLkj.setReadonly(LkjDataLine.EDITABLE);
        rightLkj.setLkjGroupId(previousLkjDataLine.getLkjGroupId());
        rightLkj.setSeq(rightLkj.getSeq() - 1);
        lkjDataLines.stream().filter(x -> x.getSeq() <= rightLkj.getSeq()).forEach(x -> x.setSeq(x.getSeq() - 1)); // 后面的排序依次 + 1
        lkjDataLines.add(rightLkj);

        // 3.去掉previousLkjDataLineId对应的新生成的那条lkjDataLine
        LkjDataLine needToRemove = lkjDataLines
                .stream()
                .filter(lkjDataLine ->
                        lkjDataLine.getLeftDeviceId().equals(previousLkjDataLine.getLeftDeviceId())
                                && lkjDataLine.getRightDeviceId().equals(previousLkjDataLine.getRightDeviceId())
                )
                .reduce(null, (x, y) -> y);

        lkjDataLines.remove(needToRemove);
        lkjDataLineBaseDao.delete(needToRemove);

        lkjDataLineBaseDao.bulkInsert(lkjDataLines);
    }

    @Override
    public void insertDevice(String baseLkjDataLineId, String lkjTaskId, String deviceId) {
        LkjDataLine baseLkjDataLine = lkjDataLineBaseDao.get(LkjDataLine.class, baseLkjDataLineId);
        String lkjGroupId = baseLkjDataLine.getLkjGroupId();

        // 1.生成待修改的lkjDataLines
        List<LkjDataLine> lkjDataLines = copyGroups(lkjTaskId, lkjGroupId);

        // 2.额外生成两个lkjDataLine
        // 生成 '插入设备'到baseLkjDataLine的rightDevice间的lkjDataLine
        LkjDataLine rightLkj = new LkjDataLine(baseLkjDataLine, lkjTaskId);
        rightLkj.setLeftDeviceId(deviceId);
        rightLkj.setRightDeviceId(baseLkjDataLine.getRightDeviceId());
        rightLkj.setDistance(0);
        rightLkj.setReadonly(LkjDataLine.EDITABLE);
        rightLkj.setLkjGroupId(baseLkjDataLine.getLkjGroupId());
        rightLkj.setSeq(rightLkj.getSeq() + 1);
        lkjDataLines.stream().filter(x -> x.getSeq() >= rightLkj.getSeq()).forEach(x -> x.setSeq(x.getSeq() + 1)); // 后面的排序依次 + 1
        lkjDataLines.add(rightLkj);

        // 生成baseLkjDataLine的leftDevice到'插入设备'到间的lkjDataLine
        LkjDataLine leftLkj = new LkjDataLine(baseLkjDataLine, lkjTaskId);
        leftLkj.setLeftDeviceId(baseLkjDataLine.getLeftDeviceId());
        leftLkj.setRightDeviceId(deviceId);
        leftLkj.setDistance(0);
        leftLkj.setReadonly(LkjDataLine.EDITABLE);
        leftLkj.setLkjGroupId(baseLkjDataLine.getLkjGroupId());
        leftLkj.setSeq(leftLkj.getSeq() + 1);
        lkjDataLines.stream().filter(x -> x.getSeq() >= leftLkj.getSeq()).forEach(x -> x.setSeq(x.getSeq() + 1)); // 后面的排序依次 + 1
        lkjDataLines.add(leftLkj);

        // 3.去掉baseLkjDataLineId对应的新生成的那条lkjDataLine
        LkjDataLine needToRemove = lkjDataLines
                .stream()
                .filter(lkjDataLine ->
                        lkjDataLine.getLeftDeviceId().equals(baseLkjDataLine.getLeftDeviceId())
                                && lkjDataLine.getRightDeviceId().equals(baseLkjDataLine.getRightDeviceId())
                )
                .reduce(null, (x, y) -> y);

        lkjDataLines.remove(needToRemove);
        lkjDataLineBaseDao.delete(needToRemove);

        lkjDataLineBaseDao.bulkInsert(lkjDataLines);
    }

    @Override
    public void deleteLkj(String baseLkjDataLineId, String previousLkjDataLineId, String nextLkjDataLineId, String lkjTaskId, String deviceId) throws CustomException {
        LkjDataLine baseLkjDataLine = lkjDataLineBaseDao.get(LkjDataLine.class, baseLkjDataLineId);
        String lkjGroupId = baseLkjDataLine.getLkjGroupId();

        // 1.生成待修改的lkjDataLines
        List<LkjDataLine> lkjDataLines = copyGroups(lkjTaskId, lkjGroupId);

        String baseLeftDeviceId = baseLkjDataLine.getLeftDeviceId();
        String baseRightDeviceId = baseLkjDataLine.getRightDeviceId();

        if (baseLeftDeviceId.equals(deviceId)) {
            LkjDataLine previousLkjDataLine = lkjDataLineBaseDao.get(LkjDataLine.class, previousLkjDataLineId);
            String previousRightDeviceId = previousLkjDataLine.getRightDeviceId();
            String previousLeftDeviceId = previousLkjDataLine.getLeftDeviceId();

            // 2.找到baseLkjDataLine 和 previousLkjDataLine在新建的lkjDataLines中对应的lkjDataLine，并删除
            LkjDataLine newBsaeLkjDataLine = null;
            LkjDataLine newPreviousLkjDataLine = null;
            for (LkjDataLine lkjDataLine : lkjDataLines) {
                if (lkjDataLine.getRightDeviceId().equals(baseRightDeviceId) && lkjDataLine.getLeftDeviceId().equals(baseLeftDeviceId))
                    newBsaeLkjDataLine = lkjDataLine;
                if (lkjDataLine.getRightDeviceId().equals(previousRightDeviceId) && lkjDataLine.getLeftDeviceId().equals(previousLeftDeviceId))
                    newPreviousLkjDataLine = lkjDataLine;
            }
            lkjDataLines.remove(newBsaeLkjDataLine);
            lkjDataLines.remove(newPreviousLkjDataLine);
            lkjDataLineBaseDao.delete(newBsaeLkjDataLine);
            lkjDataLineBaseDao.delete(newPreviousLkjDataLine);

            // 3.用previousLeftDeviceId和baseRightDeviceId连成一个新的lkj，添加
            LkjDataLine insert = new LkjDataLine(baseLkjDataLine, lkjTaskId);

            insert.setLeftDeviceId(previousLeftDeviceId);
            insert.setRightDeviceId(baseRightDeviceId);
            insert.setDistance(0);
            insert.setReadonly(LkjDataLine.EDITABLE);
            insert.setLkjGroupId(baseLkjDataLine.getLkjGroupId());
            insert.setSeq(baseLkjDataLine.getSeq());
            lkjDataLines.add(insert);

            lkjDataLineBaseDao.bulkInsert(lkjDataLines);
            return;
        }

        // 跟上面差不多，上面删左，这个删右
        if (baseRightDeviceId.equals(deviceId)) {
            LkjDataLine nextLkjDataLine = lkjDataLineBaseDao.get(LkjDataLine.class, nextLkjDataLineId);
            String nextRightDeviceId = nextLkjDataLine.getRightDeviceId();
            String nextLeftDeviceId = nextLkjDataLine.getLeftDeviceId();

            // 2.找到baseLkjDataLine 和 nextLkjDataLine在新建的lkjDataLines中对应的lkjDataLine，并删除
            LkjDataLine newBsaeLkjDataLine = null;
            LkjDataLine newNextLkjDataLine = null;
            for (LkjDataLine lkjDataLine : lkjDataLines) {
                if (lkjDataLine.getRightDeviceId().equals(baseRightDeviceId) && lkjDataLine.getLeftDeviceId().equals(baseLeftDeviceId))
                    newBsaeLkjDataLine = lkjDataLine;
                if (lkjDataLine.getRightDeviceId().equals(nextRightDeviceId) && lkjDataLine.getLeftDeviceId().equals(nextLeftDeviceId))
                    newNextLkjDataLine = lkjDataLine;
            }
            lkjDataLines.remove(newBsaeLkjDataLine);
            lkjDataLines.remove(newNextLkjDataLine);
            lkjDataLineBaseDao.delete(newBsaeLkjDataLine);
            lkjDataLineBaseDao.delete(newNextLkjDataLine);

            // 3.用previousLeftDeviceId和baseRightDeviceId连成一个新的lkj，添加
            LkjDataLine insert = new LkjDataLine(baseLkjDataLine, lkjTaskId);

            insert.setLeftDeviceId(baseLeftDeviceId);
            insert.setRightDeviceId(nextRightDeviceId);
            insert.setDistance(0);
            insert.setReadonly(LkjDataLine.EDITABLE);
            insert.setLkjGroupId(baseLkjDataLine.getLkjGroupId());
            insert.setSeq(baseLkjDataLine.getSeq());
            lkjDataLines.add(insert);

            lkjDataLineBaseDao.bulkInsert(lkjDataLines);
            return;
        }

        throw new CustomException(new Json(JsonMessage.SYS_ERROR, "找不到对应的设备"));
    }

    @Override
    public void updateLkj(String baseLkjDataLineId, String lkjTaskId, double distance) {
        LkjDataLine baseLkjDataLine = lkjDataLineBaseDao.get(LkjDataLine.class, baseLkjDataLineId);
        String lkjGroupId = baseLkjDataLine.getLkjGroupId();

        // 1.生成待修改的lkjDataLines
        List<LkjDataLine> lkjDataLines = copyGroups(lkjTaskId, lkjGroupId);

        // 2.找到待测量的lkjDataLine
        for (LkjDataLine lkjDataLine : lkjDataLines) {
            if (
                    lkjDataLine.getLeftDeviceId().equals(baseLkjDataLine.getLeftDeviceId())
                    && lkjDataLine.getRightDeviceId().equals(baseLkjDataLine.getRightDeviceId())
                ) {
                lkjDataLine.setDistance(distance);
                lkjDataLine.setReadonly(LkjDataLine.EDITABLE);
            }
        }

        lkjDataLineBaseDao.bulkInsert(lkjDataLines);
    }

    /**
     * 生成待修改的lkjDataLines
     * <p>lkjDataLine中lkjGroupId与baseLkjDataLine的lkjGroupId相同的，要生成一组作为新的任务，注意：</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp;在复制之前，需要查询LkjDataLine，字段lkjTaskId与参数中lkjTaskId相同并且</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;字段lkjGroupId与baseLkjDataLine.lkjGroupId相同</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp;如果存在这样的LkjDataLine，则表示已经生成过，不需要再次生成</p>
     */
    private List<LkjDataLine> copyGroups(String lkjTaskId, String lkjGroupId) {
        List<LkjDataLine> lkjDataLines = lkjDataLineBaseDao.find(
                "from LkjDataLine where lkjTaskId = :lkjTaskId and lkjGroupId = :lkjGroupId",
                Map.of("lkjTaskId", lkjTaskId, "lkjGroupId", lkjGroupId)
        );

        if (lkjDataLines.size() > 0)
            return lkjDataLines;

        // 如果没有对应的，查询原有的lkjGroupId相同且已提交未过时的lkjDataLine，重新生成一份并返回(不插入数据库)
        lkjDataLines = lkjDataLineBaseDao.find(
                "from LkjDataLine where lkjGroupId = :lkjGroupId and outdated = :outdated and approveStatus = :approveStatus",
                Map.of("lkjGroupId", lkjGroupId, "outdated", LkjDataLine.USING, "approveStatus", LkjDataLine.APPROVED)
        );

        List<LkjDataLine> returnList = new ArrayList<>();
        for (LkjDataLine lkjDataLine : lkjDataLines) {
            LkjDataLine createdLkjDataLine = new LkjDataLine(lkjDataLine, lkjTaskId);
            createdLkjDataLine.setReadonly(LkjDataLine.READ_ONLY);
            returnList.add(createdLkjDataLine);
        }

        return returnList;
    }

}
