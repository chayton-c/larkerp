package com.yingda.lkj.controller.app;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.StationService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.DESUtil;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.math.NumberUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hood  2020/2/17
 */
@Controller
@RequestMapping("/app/init")
public class AppInitController extends BaseController {

    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private OrganizationClientService organizationClientService;

    @RequestMapping(value = "/users", produces = "application/json")
    @ResponseBody
    public Json getUsers() throws Exception {
        List<User> users = userBaseService.getAllObjects(User.class);
        List<EncryptedUser> returnList = new ArrayList<>();

        for (User user : users) {
            if (Role.ADMIN.equals(user.getUserName()))
                continue;

            EncryptedUser encrypted = new EncryptedUser();

            encrypted.setUserId(user.getId());
            encrypted.setUserName(user.getUserName());
//            encrypted.setPassword(DESUtil.encrypt(user.getPassword()));
            encrypted.setPassword(user.getPassword());
            encrypted.setDisplayName(user.getDisplayName());

            String sectionId = user.getSectionId();
            encrypted.setSectionId(sectionId);
            encrypted.setSectionName(organizationClientService.getById(sectionId).getName());

            String workshopId = user.getWorkshopId();
            encrypted.setWorkshopId(workshopId);
            encrypted.setWorkshopName(organizationClientService.getById(workshopId).getName());

            String workAreaId = user.getWorkAreaId();
            encrypted.setWorkAreaId(workAreaId);
            encrypted.setWorkAreaName(organizationClientService.getById(workAreaId).getName());

            returnList.add(encrypted);
        }

        return new Json(JsonMessage.SUCCESS, returnList);
    }

    @Data
    static class EncryptedUser {
        private String userId;
        private String userName;
        private String password;
        private String displayName;
        private String workAreaId;
        private String workAreaName;
        private String sectionId;
        private String sectionName;
        private String workshopId;
        private String workshopName;
    }
}
