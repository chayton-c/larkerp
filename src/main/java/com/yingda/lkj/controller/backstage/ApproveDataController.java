package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hood  2020/5/29
 */
@Controller
@RequestMapping("/backstage/approveData")
public class ApproveDataController extends BaseController {

    /**
     * lkj查看版本页
     */
    @RequestMapping("/versionRouting")
    public String versionRouting() {
        String dataTypeId = req.getParameter("dataTypeId");

        if (StringUtils.isEmpty(dataTypeId))
            return ApproveDataType.values()[0].getVersionRouting();

        return ApproveDataType.getById(dataTypeId).getVersionRouting();
    }

    /**
     * lkj数据详情页
     */
    @RequestMapping("/infoPageRouting")
    public String infoPageRouting() {
        String dataTypeId = req.getParameter("dataTypeId");

        if (StringUtils.isEmpty(dataTypeId))
            return ApproveDataType.values()[0].getInfoPageRouting();

        return ApproveDataType.getById(dataTypeId).getInfoPageRouting();
    }

    /**
     * lkj审批详情页
     */
    @RequestMapping("/approveFlowInfoRouting")
    public String approveFlowInfoRouting() {
        String dataTypeId = req.getParameter("dataTypeId");

        if (StringUtils.isEmpty(dataTypeId))
            return ApproveDataType.values()[0].getApproveFlowInfoRouting();

        return ApproveDataType.getById(dataTypeId).getApproveFlowInfoRouting();
    }
}
