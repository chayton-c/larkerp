package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveConfig;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveConfigService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.backstage.organization.OrganizationService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.BeanUtils;
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
 * <p>人员组织页(局，站段，车间，工区管理)</p>
 * <p>除了组织管理页，其他页面的controller不要调用这里的方法</p>
 *
 * @author hood  2019/12/26
 */
@Controller
@RequestMapping("/backstage/organization")
public class OrganizationController extends BaseController {

    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private DataApproveConfigService dataApproveConfigService;
    @Autowired
    private BaseService<User> userBaseService;

    private Organization pageOrganization;

    @ModelAttribute
    public void setPageOrganization(Organization pageOrganization) {
        this.pageOrganization = pageOrganization;
    }

    /**
     * 组织列表
     */
    @RequestMapping("")
    @ResponseBody
    public Json getAll() {
        Map<String, Object> attributes = new HashMap<>();

        List<Organization> completeTree; // 站段及以下
        completeTree = organizationService.getCompleteTree();

        attributes.put("organizations", completeTree);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    /**
     * 验证角色名是否重复
     */
    @PostMapping("/checkOrganization")
    @ResponseBody
    public Json checkOrganization() throws CustomException {
        checkParameters("name");
        String id = req.getParameter("id");
        String organizationName = req.getParameter("name");

        Organization organizationByName = organizationService.getByName(organizationName);

        if (organizationByName == null)
            return new Json(JsonMessage.SUCCESS);

        // 新增时
        if (StringUtils.isEmpty(id))
            return new Json(JsonMessage.DUPLICATE_DATA, "输入的单位名已存在");

        // 修改时
        // 修改的单位
        Organization original = organizationService.getById(id);
        if (original.getName().equals(organizationName))
            return new Json(JsonMessage.SUCCESS);

        return new Json(JsonMessage.DUPLICATE_DATA, "输入的单位名已存在");
    }

    /**
     * 获取下级组织
     */
    @RequestMapping("/getSubordinateOrganization")
    @ResponseBody
    public Json getSubordinateOrganization() {
        Map<String, Object> attributes = new HashMap<>();

        String name = req.getParameter("name");
        String checkedOrganizationId = req.getParameter("checkedOrganizationId");
        Integer pageSize = page.getPageSize();
        Integer currentPage = page.getCurrentPage();

        List<Organization> organizations = organizationClientService.getSlave(checkedOrganizationId).stream()
                .sorted(Comparator.comparingLong(x -> -x.getUpdateTime().getTime()))
                .collect(Collectors.toList());
        if (StringUtils.isNotEmpty(name))
            organizations = organizations.stream().filter(x -> x.getName().contains(name)).collect(Collectors.toList());

        Stream<Organization> organizationStream = organizations.stream();
        organizationStream = organizationStream.skip((currentPage - 1) * pageSize).limit(pageSize);

        attributes.put("subordinateOrganizations", organizationStream.collect(Collectors.toList()));
        page.setDataTotal((long) organizations.size());
        attributes.put("page", page);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    /**
     * 菜单详情页
     */
    @RequestMapping("/info")
    @ResponseBody
    public Json info() throws CustomException {
        Map<String, Object> attributes = new HashMap<>();
        String parentOrganizationId = req.getParameter("parentOrganizationId");
        String id = req.getParameter("organizationId");
        if (StringUtils.isEmpty(id) && StringUtils.isEmpty(parentOrganizationId))
            throw new CustomException(new Json(JsonMessage.DATA_NO_COMPLETE));

        Organization organization = StringUtils.isNotEmpty(id) ? organizationService.getById(id) : new Organization();

        if (StringUtils.isNotEmpty(id))
            parentOrganizationId = organization.getParentId();
        else
            organization.setParentId(parentOrganizationId);

        Organization parentOrganization = organizationClientService.getById(parentOrganizationId);
        List<Organization> parentOrganizations = organizationClientService.getByLevel(parentOrganization.getLevel());
        attributes.put("organization", organization);
        attributes.put("parentOrganizations", parentOrganizations);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws CustomException {
        Timestamp current = new Timestamp(System.currentTimeMillis());
        String lkjApproveConfig = req.getParameter("lkjApproveConfig");
        String parentId = pageOrganization.getParentId();

        Organization parentOrganization = organizationClientService.getById(parentId);
        String id = pageOrganization.getId();
        Organization organization;
        if (StringUtils.isEmpty(id))
            organization = new Organization(pageOrganization, parentOrganization);
        else
            organization = organizationClientService.getById(id);

        BeanUtils.copyProperties(pageOrganization, organization, "id", "shortName", "level", "code");
        organization.setUpdateTime(current);
        organizationService.saveOrUpdate(organization);

        // 如果上传了审批流程数据，保存，暂时没有审批逻辑
        if (StringUtils.isNotEmpty(lkjApproveConfig))
            dataApproveConfigService.saveDataApproveList(dataApproveConfigService.parse(lkjApproveConfig, pageOrganization.getId()));

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Json delete() throws Exception {
        String organizationIds = req.getParameter("organizationIds");

        List<User> users = userBaseService.find(
                """
                        from User
                        where bureauId in :organizationIds or sectionId in :organizationIds or workshopId in :organizationIds or workAreaId in :organizationIds
                        """,
                Map.of("organizationIds", organizationIds)
        );

        if (!users.isEmpty())
            throw new CustomException(JsonMessage.CONTAINING_ASSOCIATED_DATA, "仍有用户使用指定岗位，请在修改对应角色的岗位后再尝试删除");

        String[] organizationIdArr = organizationIds.split(",");
        for (String organizationId : organizationIdArr) {
            List<Organization> slave = organizationClientService.getSlave(organizationId);
            if (!slave.isEmpty())
                throw new CustomException(JsonMessage.CONTAINING_ASSOCIATED_DATA, "指定组织下仍有下级，请在修改对应组织后再尝试删除");
        }

        // asList出来的list不能addAll，转成ArrayList才能
        organizationService.delete(new ArrayList<>(Arrays.asList(organizationIdArr)));

        return new Json(JsonMessage.SUCCESS);
    }

}
