package com.yingda.lkj.controller.client;

import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author hood  2020/1/3
 */
@RequestMapping("/client/organization")
@Controller
public class OrganizationClientController extends BaseController {

    @Autowired
    private OrganizationClientService organizationClientService;

    /**
     * 拿Organization(局，段。。。)的下级节点
     */
    @RequestMapping("/getSlave")
    @ResponseBody
    public Json getSlave(String organizationId) throws CustomException {
        String sectionId = getSectionId();

        List<Organization> slaves = organizationClientService.getSlave(organizationId);


        for (Organization slave : slaves) // 非管理员筛选站段时，获取当前用户所在站段
            if (slave.getLevel() == Organization.SECTION && StringUtils.isNotEmpty(sectionId) && !isAdmin())
                return new Json(JsonMessage.SUCCESS, List.of(organizationClientService.getById(sectionId)));

        return new Json(JsonMessage.SUCCESS, slaves);
    }


}
