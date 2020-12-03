package com.yingda.lkj.controller.app.init;

import com.yingda.lkj.beans.entity.backstage.line.Station;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.pojo.app.AppStation;
import com.yingda.lkj.beans.pojo.app.AppWorkshop;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hood  2020/4/13
 */
@Controller
@RequestMapping("/app/init/workshops")
public class InitWorkshopController extends BaseController {
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private BaseService<User> userBaseService;

    @RequestMapping("")
    @ResponseBody
    public Json getData() throws Exception {
        String userId = req.getParameter("userId");
        User user = userBaseService.get(User.class, userId);

        Organization workshop = organizationClientService.getById(user.getWorkshopId());
        List<AppWorkshop> workshops = List.of(workshop).stream().map(AppWorkshop::new).collect(Collectors.toList());
        return new Json(JsonMessage.SUCCESS, workshops);
    }
}
