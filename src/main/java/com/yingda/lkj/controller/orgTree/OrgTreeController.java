package com.yingda.lkj.controller.orgTree;

import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.controller.orgTree.util.LayuiUtil;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.backstage.organization.OrganizationService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.service.system.RoleService;
import com.yingda.lkj.utils.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/backstage/tree")
public class OrgTreeController extends BaseController {

    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private BaseService<Organization> organizationBaseService;
    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private RoleService roleService;



    /**组织树页加载**/
    @RequestMapping("/orgInfo")
    public ModelAndView OrgInfo() {
        return new ModelAndView("/backstage/organization/orgTree");
    }


    /**节点信息**/
    @RequestMapping("/testTree")
    @ResponseBody
    public Json getAll() {
        User user = RequestUtil.getUser(req);

        List<Organization> completeTree; // 站段及以下
        if ("admin".equals(user.getUserName()))
            completeTree = organizationService.getCompleteTree();
        else
            completeTree = organizationService.getCompleteTree(user.getSectionId());

        List<Organization> expanded = organizationService.expand(completeTree, new ArrayList<>());
        for (Organization organization : expanded) {
            List<Organization> slaves = organization.getOrganizationList();
            organization.setHasSlave(slaves != null && slaves.size() > 0);
            organization.setOrganizationList(null);
        }

        expanded.forEach(x -> x.setOrganizationList(null)); // 下级没用，放进去包就太大了

        return new Json(JsonMessage.SUCCESS, expanded);
    }

    //加载 父级 table信息
    @RequestMapping(value = "parentInfo", produces = "application/json")
    @ResponseBody
    public Map<String, Object> parentInfo(int page, int limit) throws Exception{

        String nodeId = req.getParameter("nodeId");

        Map<String, Object> params = new HashMap<>();
        params.put("nodeId", nodeId);
        List<Organization> total = organizationBaseService.find("from Organization where id = :nodeId", params);

        List<Organization> organizations = organizationBaseService.find("from Organization where id = :nodeId", params, page, limit);

        for (Organization organization : organizations) {
            List<Organization> slaves = organization.getOrganizationList();
            organization.setHasSlave(slaves != null && slaves.size() > 0);
            organization.setOrganizationList(null);
        }

        organizations.forEach(x -> x.setOrganizationList(null));
        LayuiUtil data = LayuiUtil.data(total.size(), organizations);

        return data;

    }

    //加载 子级 table信息
    @RequestMapping(value = "childrenInfo", produces = "application/json")
    @ResponseBody
    public Map<String, Object> childrenInfo(int page, int limit) throws Exception{

        String nodeId = req.getParameter("nodeId");

        Map<String, Object> params = new HashMap<>();
        params.put("nodeId", nodeId);
        List<Organization> total = organizationBaseService.find("from Organization where parentId = :nodeId", params);

        List<Organization> organizations = organizationBaseService.find("from Organization where parentId = :nodeId", params, page, limit);

        LayuiUtil data = LayuiUtil.data(total.size(), organizations);

        return data;

    }


    //工区加载 用户
    @RequestMapping(value = "childrenStudentInfo", produces = "application/json")
    @ResponseBody
    public Map<String, Object> childrenStudentInfo(int page, int limit) throws Exception{

        String nodeId = req.getParameter("nodeId");

        Organization organization = organizationClientService.getById(nodeId);

        String workAreaId = organization.getId();

        List<User> total = userBaseService.find("from User where workAreaId = :workAreaId", Map.of("workAreaId", workAreaId));

        List<User> users = userBaseService.find("from User where workAreaId = :workAreaId", Map.of("workAreaId", workAreaId), page, limit);
        for (User user : users) {
            String userSectionId = user.getSectionId();
            String userWorkshopId = user.getWorkshopId();
            String userWorkAreaId = user.getWorkAreaId();

            Organization section = organizationClientService.getById(userSectionId);
            Organization workshop = organizationClientService.getById(userWorkshopId);
            Organization workArea = organizationClientService.getById(userWorkAreaId);
            Role role = roleService.getRole(user.getRoleId());

            user.setSectionName(section.getName());
            user.setWorkshopName(workshop.getName());
            user.setWorkAreaName(workArea.getName());
            user.setRoleName(role.getName());
        }

        LayuiUtil data = LayuiUtil.data(total.size(), users);

        return data;

    }

}
