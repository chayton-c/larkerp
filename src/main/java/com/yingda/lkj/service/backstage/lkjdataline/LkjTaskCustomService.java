package com.yingda.lkj.service.backstage.lkjdataline;

import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.exception.CustomException;

/**
 * 手动生成(区别于导入)lkj任务接口
 *
 * @author hood  2020/3/26
 */
public interface LkjTaskCustomService {

    /**
     * <p>在baseLkjDataLine后添加设备形成lkj</p>
     * <p>在baseLkjDataLine和nextLkjDataLine间插入deviceId对应的设备形成新的lkjDataLine（下面称为插入设备）</p>
     * <p>1.生成待修改的lkjDataLines</p>
     * <p>&nbsp;&nbsp;lkjDataLine中lkjGroupId与baseLkjDataLine的lkjGroupId相同的，要生成一组作为新的任务，注意：</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp;在复制之前，需要查询LkjDataLine，字段lkjTaskId与参数中lkjTaskId相同并且</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;字段lkjGroupId与baseLkjDataLine.lkjGroupId相同</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp;如果存在这样的LkjDataLine，则表示已经生成过，不需要再次生成</p>
     * <p>2.在新生成的这组lkjDataLine中额外生成两个lkjDataLine，分别是:</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp;(1)baseLkjDataLine的rightDevice到'插入设备'间的lkjDataLine和</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp;(2)'插入设备'到nextLkjDataLine的rightDevice间的lkjDataLine</p>
     * <p>3.去掉nextLkjDataLineId对应的新生成的那条kjDataLine</p>
     *
     * @param baseLkjDataLineId 在baseLkjDataLine和nextLkjDataLine间插入设备形成新的lkjDataLine
     * @param nextLkjDataLineId 在baseLkjDataLine和nextLkjDataLine间插入设备形成新的lkjDataLine
     * @param lkjTaskId         插入的lkjDataLine在该任务下
     * @param deviceId          插入的设备
     */
    void appendDevice(String baseLkjDataLineId, String nextLkjDataLineId, String lkjTaskId, String deviceId);

    /**
     * 在baseLkjDataLine前添加设备形成lkj
     * 跟上一个方法差不多，一个前一个后
     */
    void prependDevice(String baseLkjDataLineId, String previousDataLineId, String lkjTaskId, String deviceId);

    /**
     * 在baseLkjDataLine间插入设备形成lkj
     * 跟上一个方法差不多，在中间插入，形成两个lkj,再把原来的删掉
     */
    void insertDevice(String baseLkjDataLineId, String lkjTaskId, String deviceId);

    /**
     * 删除lkj的某个设备节点
     * 跟上一个方法差不多，先新建任务，然后删除baseLkjDataLine上的deviceId对应的设备
     * 在把删掉的跟前面/后面的lkj连上
     *
     */
    void deleteLkj(String baseLkjDataLineId, String previousLkjDataLineId, String nextLkjDataLineId, String lkjTaskId, String deviceId) throws CustomException;

    void updateLkj(String baseLkjDataLineId, String lkjTaskId, double distance);
}
