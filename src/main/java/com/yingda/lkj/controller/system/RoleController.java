package com.yingda.lkj.controller.system;

import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.RoleOrganization;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.system.RoleOrganizationService;
import com.yingda.lkj.service.system.RoleService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <span>角色管理页</span>
 * <span>角色修改，因为是跟授权一起的，所以放在AuthController里了</span>
 *
 * @author hood  2019/12/18
 */
@Controller
@RequestMapping("/role")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private RoleOrganizationService roleOrganizationService;

    private Role role;

    /**
     * 后端自用角色列表
     */
    @RequestMapping("/edit")
    public ModelAndView roleEdit() {
        return new ModelAndView("system/role", Map.of("roles", roleService.showDown()));
    }

    /**
     * 角色列表
     */
    @RequestMapping("")
    @ResponseBody
    public Json role() {
        Map<String, Object> attributes = new HashMap<>();

        // TODO 改成用sql

        String name = req.getParameter("name");
        String checkedLevel = req.getParameter("checkedLevel");
        Integer pageSize = page.getPageSize();
        Integer currentPage = page.getCurrentPage();

        List<Role> roles = roleService.showDown().stream().sorted(Comparator.comparingLong(x -> -x.getUpdateTime().getTime())).collect(Collectors.toList());
        if (StringUtils.isNotEmpty(name))
            roles = roles.stream().filter(x -> x.getName().contains(name)).collect(Collectors.toList());
        // checkedLevel表示前端回传的组织列表中选中的部门等级，第一级的level为0，所以在比较时要加一
        if (StringUtils.isNotEmpty(checkedLevel))
            roles = roles.stream().filter(x -> x.getOrganizationPermission() == (Integer.parseInt(checkedLevel) + 1)).collect(Collectors.toList());

        Stream<Role> roleStream = roles.stream();
        roleStream = roleStream.skip((currentPage - 1) * pageSize).limit(pageSize);

        List<Role> result = roleStream.collect(Collectors.toList());
        for (Role item : result) {
            List<RoleOrganization> roleOrganizations = roleOrganizationService.getByRoleId(item.getId());
            List<String> organizationIds = StreamUtil.getList(roleOrganizations, RoleOrganization::getOrganizationId);
            List<Organization> organizations = organizationClientService.getByIds(organizationIds);
            item.setOrganizationNames(organizations.stream().map(Organization::getName).collect(Collectors.joining(",")));
        }

        attributes.put("roles", result);
        page.setDataTotal((long) roles.size());
        attributes.put("page", page);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/getByOrganizationId")
    @ResponseBody
    public Json getByOrganizationId() {
        Map<String, Object> attributes = new HashMap<>();

        String organizationId = req.getParameter("organizationId");
        Organization organization = organizationClientService.getById(organizationId);
        byte level = organization.getLevel();

        List<Role> roles = roleService.showDown().stream()
                .filter(x -> x.getOrganizationPermission() == level)
                .collect(Collectors.toList());

        attributes.put("roles", roles);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    /**
     * 角色添加/修改，权限修改
     */
    @PostMapping("/updateRole")
    @ResponseBody
    public Json updateRole() {
        Map<String, Object> attributes = new HashMap<>();

        Timestamp current = new Timestamp(System.currentTimeMillis());
        if (StringUtils.isEmpty(role.getId())) {
            role.setId(UUID.randomUUID().toString());
            role.setAddTime(current);
        }
        role.setUpdateTime(current);
        roleService.saveOrUpdate(role);
        attributes.put("role", role);

        return new Json(JsonMessage.SUCCESS, attributes, "提交成功");
    }

    /**
     * 验证角色名是否重复
     */
    @PostMapping("/deleteRole")
    @ResponseBody
    public Json deleteRole() throws CustomException {
        checkParameters("roleIds");
        String ids = req.getParameter("roleIds");
        roleService.deleteRole(Arrays.asList(ids.split(",")));

        return new Json(JsonMessage.SUCCESS, "提交成功");
    }

    /**
     * 验证角色名是否重复
     */
    @PostMapping("/checkRole")
    @ResponseBody
    public Json checkRole() throws CustomException {
        checkParameters("name");
        String id = req.getParameter("id");
        String roleName = req.getParameter("name");

        Role roleByName = roleService.getRoleByName(roleName);

        if (roleByName == null)
            return new Json(JsonMessage.SUCCESS);

        // 新增时
        if (StringUtils.isEmpty(id))
            return new Json(JsonMessage.DUPLICATE_DATA, "输入的岗位名已存在");

        // 修改时
        // 修改的角色
        Role original = roleService.getRole(id);
        if (original.getName().equals(roleName))
            return new Json(JsonMessage.SUCCESS);

        return new Json(JsonMessage.DUPLICATE_DATA, "输入的岗位名已存在");
    }

    @ModelAttribute
    public void setRole(Role role) {
        this.role = role;
    }
}
