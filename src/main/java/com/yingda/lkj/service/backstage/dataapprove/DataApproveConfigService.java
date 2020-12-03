package com.yingda.lkj.service.backstage.dataapprove;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveConfig;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;

import java.util.List;

/**
 * @author hood  2020/5/27
 */
public interface DataApproveConfigService {

    /**
     * 用户审批时，查询用户可以提交审批的用户(下一级审批用户)
     */
    List<User> getAvailableApproveUsers(User user) throws Exception;

    void saveDataApproveList(List<DataApproveConfig> dataApproveConfigs);

    /**
     * 获取sectionId对应站段下的lkj审核配置
     */
    List<DataApproveConfig> getDataApproveConfigList(String sectionId);


    // configlist -> 工人, 科长; 科长, 段长;
    String format(List<DataApproveConfig> dataApproveConfigs);

    // 工人, 科长; 科长, 段长; -> configlist
    List<DataApproveConfig> parse(String dataApproveConfigListStr, String sectionId) throws CustomException;
}
