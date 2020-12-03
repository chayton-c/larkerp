package com.yingda.lkj.controller.system;

import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.RoleOrganization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.service.system.RoleOrganizationService;
import com.yingda.lkj.service.system.RoleService;
import com.yingda.lkj.service.system.UserService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2020/11/26
 */
@RequestMapping("/backstage/roleOrganization")
@Controller
public class RoleOrganizationController extends BaseController {
    @Autowired
    private RoleService roleService;
    @Autowired
    private RoleOrganizationService roleOrganizationService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private BaseService<Role> roleBaseService;
    @Autowired
    private BaseService<RoleOrganization> roleOrganizationBaseService;

    @RequestMapping("/add")
    @ResponseBody
    public Json add() {
        String roleId = req.getParameter("roleId");
        String organizationId = req.getParameter("organizationId");

        roleOrganizationService.add(roleId, organizationId);

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Json delete() throws Exception {
        String roleId = req.getParameter("roleId");
        String organizationId = req.getParameter("organizationId");

        List<User> users = userBaseService.find(
                """
                        FROM User 
                        WHERE roleId = :roleId 
                        AND (
                            bureauId = :organizationId 
                            OR sectionId = :organizationId 
                            OR workshopId = :organizationId 
                            OR workAreaId = :organizationId
                            )        
                            """,
                Map.of(
                        "roleId", roleId,
                        "organizationId", organizationId
                )
        );
        if (!users.isEmpty())
            throw new CustomException(JsonMessage.CONTAINING_ASSOCIATED_DATA, "存在使用该岗位的用户，请在修改对应用户岗位后再删除");


        roleOrganizationService.delete(roleId, organizationId);

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 获取该组织下已关联的所有岗位
     */
    @RequestMapping("/getRolesByOrganization")
    @ResponseBody
    public Json getRolesByOrganization() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String organizationId = req.getParameter("organizationId");
        String roleName = req.getParameter("roleName");
        Map<String, Object> params = new HashMap<>();
        String sql = """
                    SELECT
                        role.id AS id,
                        role.`name` AS `name`,
                        role.description AS description
                    FROM
                        role
                        INNER JOIN role_organization roleOrganization ON roleOrganization.role_id = role.id
                    WHERE
                        roleOrganization.organization_id = :organizationId
                """;
        params.put("organizationId", organizationId);
        if (StringUtils.isNotEmpty(roleName)) {
            sql += "AND role.name like :roleName\n";
            params.put("roleName", roleName);
        }

        List<Role> roles = roleBaseService.findSQL(
                sql, params, Role.class, page.getCurrentPage(), page.getPageSize()
        );
        List<BigInteger> count = bigIntegerBaseService.findSQL(HqlUtils.getCountSql(sql), params);
        page.setDataTotal(count);
        attributes.put("roles", roles);
        attributes.put("page", page);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    /**
     * 获取该组织下还未关联的所有同级别岗位
     */
    @RequestMapping("/getRestRolesByOrganizationId")
    @ResponseBody
    public Json getRestRolesByOrganizationId() throws Exception {
        Map<String, Object> attributes = new HashMap<>();

        String organizationId = req.getParameter("organizationId");
        Organization organization = organizationClientService.getById(organizationId);
        byte level = organization.getLevel();

        List<Role> sameLevelRoles = roleService.getByOrganizationPermission(level);
        List<RoleOrganization> roleOrganizations = roleOrganizationService.getByOrganizationId(organizationId);
        List<String> createdRoleIds = StreamUtil.getList(roleOrganizations, RoleOrganization::getRoleId);

        List<Role> result = sameLevelRoles.stream().filter(x -> !createdRoleIds.contains(x.getId())).collect(Collectors.toList());
        attributes.put("roles", result);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

}
