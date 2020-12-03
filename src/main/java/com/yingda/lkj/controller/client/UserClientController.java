package com.yingda.lkj.controller.client;

import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/3/12
 */
@Controller
@RequestMapping("/client/user")
public class UserClientController extends BaseController {
    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private OrganizationClientService organizationClientService;

    @RequestMapping("/getUserByWorkAreaId")
    @ResponseBody
    public Json getUserByWorkAreaId(String workAreaId) throws Exception {
        User user = RequestUtil.getUser(req);

        List<User> users = null;
        if (StringUtils.isNotEmpty(workAreaId))
            users = userBaseService.find("from User where workAreaId = :workAreaId", Map.of("workAreaId", workAreaId));
        else if (user != null)
            users = userBaseService.find("from User where sectionId = :sectionId", Map.of("sectionId", user.getSectionId()));
        return new Json(JsonMessage.SUCCESS, users);
    }

    @RequestMapping("/getWorkAreaByUserId")
    @ResponseBody
    public Json getWorkAreaByUserId(String userId) throws Exception {
        User loginUser = RequestUtil.getUser(req);

        List<Organization> workAreas = null;
        if (StringUtils.isNotEmpty(userId)) {
            User user = userBaseService.get(User.class, userId);
            String workAreaId = user.getWorkAreaId();
            workAreas = List.of(organizationClientService.getById(workAreaId));
        } else if (loginUser != null) {
            workAreas = organizationClientService.getWorkAreasBySectionId(loginUser.getSectionId());
        }
        return new Json(JsonMessage.SUCCESS, workAreas);
    }


}
