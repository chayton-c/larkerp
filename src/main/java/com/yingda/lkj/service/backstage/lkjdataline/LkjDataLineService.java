package com.yingda.lkj.service.backstage.lkjdataline;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveFlow;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersion;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjTask;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.lkj.LkjDataLineFromExcel;
import com.yingda.lkj.beans.pojo.device.Semaphore;

import java.util.List;

/**
 * lkjDataLine数据操作
 *
 * @author hood  2020/1/7
 */
public interface LkjDataLineService {
    /**
     * 展开lkj数据为以信号机为主体的列表pojo
     */
    List<Semaphore> expandLkjDataLine(List<LkjDataLine> lkjDataLines) throws Exception;

    /**
     * 为lkjDataLine填充信号机
     */
    List<LkjDataLine> fillLkjDataLineDevice(List<LkjDataLine> lkjDataLines) throws Exception;

    /**
     * 生成待审批的lkjDataLine
     */
    List<LkjDataLine> createLkjDataLine(DataApproveFlow dataApproveFlow, List<LkjDataLine> rawLkjDataLines);

    /**
     * 导入任务对应的lkjDataLine
     */
    List<LkjDataLine> createLkjDataLine(LkjTask lkjTask, List<LkjDataLine> rawLkjDataLines);

    List<LkjDataLine> wrapLkjDataLine(List<LkjDataLineFromExcel> lkjDataLineFromExcels, String fragmentId) throws CustomException;

    /**
     * 为未完成的任务填写lkj距离
     */
    void fillTask(String lkjTaskId, List<LkjDataLine> rawLkjDataLines);

    /**
     * 修改dataApproveFlow下的lkj数据为未通过
     */
    void refuseLkjDataLines(DataApproveFlow dataApproveFlow);

    /**
     * 提交审批流下的数据为已完成
     */
    void completeLkjDataLine(DataApproveFlow dataApproveFlow);
}
