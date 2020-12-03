package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.Menu;
import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.utils.ExcelRowInfo;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.service.system.AuthService;
import com.yingda.lkj.service.system.MenuService;
import com.yingda.lkj.service.system.RoleService;
import com.yingda.lkj.service.system.UserService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.date.DateUtil;
import com.yingda.lkj.utils.excel.ExcelUtil;
import com.yingda.lkj.utils.hql.HqlUtils;
import com.yingda.lkj.utils.pojo.PojoUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理页
 *
 * @author hood  2019/12/27
 */
@Controller
@RequestMapping("/backstage/user")
public class UserController extends BaseController {

    @Autowired
    private BaseService<User> userBaseService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private AuthService authService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private UserService userService;
    @Autowired
    private BaseService<Role> roleBaseService;

    private User pageUser;

    @ModelAttribute
    public void setPageUser(User pageUser) {
        this.pageUser = pageUser;
    }

    @RequestMapping("")
    @ResponseBody
    public Json userList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        User user = getUser();

        byte organizationPermission = getOrganizationPermission();
        String displayName = req.getParameter("displayName");
        String roleName = req.getParameter("roleName");
        String roleId = req.getParameter("roleId");
        String checkedOrganizationId = req.getParameter("organizationId");

        Map<String, Object> params = new HashMap<>();
        String sql = """
                SELECT
                	user.id AS id,
                	user.display_name AS displayName,
                	user.user_name AS userName,
                	user.add_time AS addTime,
                	role.name AS roleName,
                	bureau.name AS bureauName,
                	section.name AS sectionName,
                	workshop.name AS workshopName,
                	workArea.name AS workAreaName
                FROM
                	user
                	LEFT JOIN role ON user.role_id = role.id
                	LEFT JOIN organization AS bureau ON user.bureau_id = bureau.id
                	LEFT JOIN organization AS section ON user.section_id = section.id
                	LEFT JOIN organization AS workshop ON user.workshop_id = workshop.id
                	LEFT JOIN organization AS workArea ON user.work_area_id = workArea.id 
                WHERE
                    1 = 1
                """;
        if (StringUtils.isNotEmpty(checkedOrganizationId)) {
            // 局长只有bureauId没有sectionId，段长有sectionId而没有workshopId
            sql += """
                    AND (
                            (user.bureau_id = :checkedOrganizationId AND user.section_id IS NULL)
                         OR (user.section_id = :checkedOrganizationId AND user.workshop_id IS NULL)
                         OR (user.workshop_id = :checkedOrganizationId AND user.work_area_id IS NULL)
                         OR (user.work_area_id = :checkedOrganizationId)
                         )
                    """;
            params.put("checkedOrganizationId", checkedOrganizationId);
        }
        if (StringUtils.isNotEmpty(roleId)) {
            sql += "AND role.id = :roleId\n";
            params.put("roleId", roleId);
        }
        if (StringUtils.isNotEmpty(displayName)) {
            sql += "AND user.display_name like :displayName\n";
            params.put("displayName", "%" + displayName + "%");
        }
        if (StringUtils.isNotEmpty(roleName)) {
            sql += "AND role.name like :roleName\n";
            params.put("roleName", "%" + roleName + "%");
        }

        // 如果授权信息到车间，只能看本车间的数据
        if (organizationPermission == Role.SECTION) {
            sql += "AND user.section_id = :userSectionId\n";
            params.put("userSectionId", user.getSectionId());
        }
        if (organizationPermission == Role.WORKSHOP) {
            sql += "AND user.workshop_id = :userWorkshopId\n";
            params.put("userWorkshopId", user.getWorkshopId());
        }

        sql += "ORDER BY user.update_time desc";

        List<User> users = userBaseService.findSQL(sql, params, User.class, page.getCurrentPage(), page.getPageSize());

        // 查询总数
        List<BigInteger> count = bigIntegerBaseService.findSQL(HqlUtils.getCountSql(sql), params);
        page.setDataTotal(count.isEmpty() ? 0 : count.get(0).longValue());

        attributes.put("users", users);
        attributes.put("page", page);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/infoPage")
    @ResponseBody
    public Json infoPage() throws Exception {
        String id = req.getParameter("userId");
        String checkedOrganizationId = req.getParameter("checkedOrganizationId");

        User user = StringUtils.isEmpty(id) ? new User() : userBaseService.get(User.class, id);
        setUserOrganizationByCheckedOrganizations(user, checkedOrganizationId);

        Organization organization = organizationClientService.getById(checkedOrganizationId);
        // 所有同级别及下级角色
        List<Role> roles = roleService.showDown().stream()
                .filter(x -> x.getOrganizationPermission() == organization.getLevel())
                .collect(Collectors.toList());
        // 备选局
        List<Organization> bereaus = organizationClientService.getBureaus();
        // 备选站段
        List<Organization> sections = organizationClientService.getSections();
        // 备选车间
        List<Organization> workshops = organizationClientService.getWorkshops();
        // 备选工区
        List<Organization> workAreas = organizationClientService.getWorkAreas();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user", user);
        attributes.put("roles", roles);
        attributes.put("bureaus", bereaus);
        attributes.put("sections", sections);
        attributes.put("workshops", workshops);
        attributes.put("workAreas", workAreas);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @PostMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() throws Exception {
        Json json = PojoUtils.checkParams(pageUser, User.REQUIRED_MAP);
        if (!json.isSuccess())
            return json;

        String id = req.getParameter("id");
        User user = StringUtils.isEmpty(id) ? pageUser = new User(pageUser) : userService.getById(id);

        String roleId = req.getParameter("roleId");
        user.setRoleId(roleId);
        user.setDisplayName(req.getParameter("displayName"));

        String bureauId = req.getParameter("bureauId");
        user.setBureauId(StringUtils.isNotEmpty(bureauId) ? bureauId : null);
        String sectionId = req.getParameter("sectionId");
        user.setSectionId(StringUtils.isNotEmpty(sectionId) ? sectionId : null);
        String workshopId = req.getParameter("workshopId");
        user.setWorkshopId(StringUtils.isNotEmpty(workshopId) ? workshopId : null);
        String workAreaId = req.getParameter("workAreaId");
        user.setWorkAreaId(StringUtils.isNotEmpty(workAreaId) ? workAreaId : null);

        // 校验用户角色等级要与所在组织保持一致
        byte organizationPermission = roleService.getRole(roleId).getOrganizationPermission();
        if (organizationPermission == Role.BUREAU && StringUtils.isNotEmpty(sectionId))
            throw new CustomException(JsonMessage.PARAM_INVALID, "岗位等级要与用户所在组织保持一致");
        if (organizationPermission == Role.SECTION && StringUtils.isNotEmpty(workshopId))
            throw new CustomException(JsonMessage.PARAM_INVALID, "岗位等级要与用户所在组织保持一致");
        if (organizationPermission == Role.WORKSHOP && StringUtils.isNotEmpty(workAreaId))
            throw new CustomException(JsonMessage.PARAM_INVALID, "岗位等级要与用户所在组织保持一致");

        pageUser.setUpdateTime(current());
        userBaseService.saveOrUpdate(user);

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 验证角色名是否重复
     */
    @PostMapping("/checkUserName")
    @ResponseBody
    public Json checkUserName() throws CustomException {
        checkParameters("userName");
        String id = req.getParameter("id");
        String userName = req.getParameter("userName");

        User userByName = userService.getUserByUserName(userName);

        if (userByName == null)
            return new Json(JsonMessage.SUCCESS);

        // 新增时
        if (StringUtils.isEmpty(id))
            return new Json(JsonMessage.DUPLICATE_DATA, "输入的登录账号已存在");

        // 修改的用户
        User original = userService.getById(id);
        if (original.getUserName().equals(userName))
            return new Json(JsonMessage.SUCCESS);

        return new Json(JsonMessage.DUPLICATE_DATA, "输入的登录账号已存在");
    }

    @RequestMapping("/deleteUser")
    @ResponseBody
    public Json deleteUser() throws Exception {
        String userIdsArr = req.getParameter("userIds");
        List<String> userIds = Arrays.asList(userIdsArr.split(","));

       userBaseService.executeHql("delete from User where id in :userIds", Map.of("userIds", userIds));

       return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/updatePassword")
    @ResponseBody
    public Json updatePassword() {
//        String oldPassword = req.getParameter("oldPassword");
//        String newPassword = req.getParameter("newPassword");
//        String repeatPassword = req.getParameter("repeatPassword");
//
//        User user = RequestUtil.getUser(req);
//        if (!StringUtils.isNotEmpty(oldPassword, newPassword, repeatPassword))
//
//        if (user.getPassword())
//        if (oldPassword.equals(user.getPassword()) && )

        return null;
    }

    //导出用户信息
    @RequestMapping("/exportUsers")
    @ResponseBody
    public void exportUsers() throws Exception {
        String displayName = pageUser.getDisplayName();
        String workshopId = pageUser.getWorkshopId() + "";
        String workAreaId = pageUser.getWorkAreaId() + "";

        Map<String, String> conditions = new HashMap<>(Map.of("userName", "!=", "sectionId", "="));
        Map<String, Object> params = new HashMap<>(Map.of("userName", "admin", "sectionId", getSectionId()));

        if (StringUtils.isNotEmpty(displayName)) {
            conditions.put("displayName", "like");
            params.put("displayName", "%" + displayName + "%");
        }
        if (StringUtils.isNotEmpty(workshopId)) {
            conditions.put("workshopId", "=");
            params.put("workshopId", workshopId);
        }
        if (StringUtils.isNotEmpty(workAreaId)) {
            conditions.put("workAreaId", "=");
            params.put("workAreaId", workAreaId);
        }

        List<User> users = userBaseService.getObjects(User.class, params, conditions, "order by addTime desc");

        for (User user : users) {
            String userSectionId = user.getSectionId();
            String userWorkshopId = user.getWorkshopId();
            String userWorkAreaId = user.getWorkAreaId();

            Organization section = organizationClientService.getById(userSectionId);
            Organization workshop = organizationClientService.getById(userWorkshopId);
            Organization workArea = organizationClientService.getById(userWorkAreaId);

            user.setSectionName(section.getName());
            user.setWorkshopName(workshop.getName());
            user.setWorkAreaName(workArea.getName());
        }

        Map<Integer, ExcelRowInfo> rowInfoMap = new HashMap<>();
        int rows = 0;
        rowInfoMap.put(rows++, new ExcelRowInfo(rows++, "用户名", "显示姓名", "电务段", "车间", "工区", "添加时间"));
        for (User user : users) {
            String userName = StringUtils.isEmpty(user.getUserName()) ? "-" : user.getUserName();
            String displayname = StringUtils.isEmpty(user.getDisplayName()) ? "-" : user.getDisplayName();
            String sectionName = StringUtils.isEmpty(user.getSectionName()) ? "-" : user.getSectionName();
            String workshopName = StringUtils.isEmpty(user.getWorkshopName()) ? "-" : user.getWorkshopName();
            String workAreaName = StringUtils.isEmpty(user.getWorkAreaName()) ? "-" : user.getWorkAreaName();
            String addTime;
            if (user.getAddTime() == null) {
                addTime = "-";
            } else {
                addTime = DateUtil.format(user.getAddTime(), "yyyy-mm-dd");
            }

            rowInfoMap.put(rows++, new ExcelRowInfo(rows++, userName, displayname, sectionName, workshopName, workAreaName, addTime));
        }

        ExcelSheetInfo excelSheetInfo = new ExcelSheetInfo("用户信息", rowInfoMap);

        Workbook workbook = ExcelUtil.createExcelFile(List.of(excelSheetInfo));
        MultipartFile multipartFile = ExcelUtil.workbook2File(workbook, "用户信息");
        export(multipartFile);
    }

    /**
     * 导入用户
     */
    @RequestMapping("/importUsers")
    @ResponseBody
    public Json importLines(MultipartFile file) throws Exception {
        User submitter = getUser();

        List<ExcelSheetInfo> excelSheetInfos = ExcelUtil.readExcelFile(file);
        if (excelSheetInfos.size() > 1)
            throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "用户excel导入时只能包含一页"));

        Map<Integer, ExcelRowInfo> rowInfoMap = excelSheetInfos.get(0).getRowInfoMap();

        String sectionId = getSectionId();
        List<Organization> workshops = organizationClientService.getSlave(sectionId);

        List<User> users = new ArrayList<>();
        // 数据从第二行开始
        for (int i = 1; i < rowInfoMap.size(); i++) {
            ExcelRowInfo excelRowInfo = rowInfoMap.get(i);
            List<String> cells = excelRowInfo.getCells();

            String userName = cells.get(0).trim();
            if (StringUtils.isEmpty(userName))
                break;
            String displayName = cells.get(1).trim();
            String workshopName = cells.get(2).trim();
            String workAreaName = cells.get(3).trim();
            String roleName = cells.get(4).trim();

            Organization workshop = workshops.stream().filter(x -> workshopName.equals(x.getName())).reduce(null, (x, y) -> y);
            if (workshop == null)
                throw new CustomException(new Json(JsonMessage.PARAM_INVALID, String.format("第%d行出现错误: 找不到名称为%s的车间", i, workshopName)));

            List<Organization> workAreas = organizationClientService.getSlave(workshop.getId());
            Organization workArea = workAreas.stream().filter(x -> workAreaName.equals(x.getName())).reduce(null, (x, y) -> y);
            if (workArea == null)
                throw new CustomException(new Json(JsonMessage.PARAM_INVALID, String.format("第%d行出现错误: 找不到名称为%s的工区", i, workAreaName)));

            Role role = roleService.getRoleByName(roleName);
            if (role == null)
                throw new CustomException(new Json(JsonMessage.PARAM_INVALID, String.format("第%d行出现错误: 找不到名称为%s的角色", i, roleName)));

            // 重名用户替换
            User duplicateNameUser = userBaseService.get(
                    "from User where userName = :userName",
                    Map.of("userName", userName)
            );
            User user = duplicateNameUser == null ? new User() : duplicateNameUser;
            if (duplicateNameUser == null) {
                user.setId(UUID.randomUUID().toString());
                user.setAddTime(current());
            }
            user.setUserName(userName);
            user.setRoleId(role.getId());
            user.setBureauId(submitter.getBureauId());
            user.setSectionId(submitter.getSectionId());
            user.setWorkshopId(workshop.getId());
            user.setWorkAreaId(workArea.getId());
            user.setDisplayName(displayName);
            user.setPassword("123456");
            user.setBanned(User.NOT_BANNED);
            user.setUpdateTime(current());
            users.add(user);
        }

        userBaseService.bulkInsert(users);
        return new Json(JsonMessage.SUCCESS, "查询到成功导入" + users.size() + "条数据");
    }

    @RequestMapping("/getVueAuthorizedMenus")
    @ResponseBody
    public Json getVueAuthorizedMenus() throws CustomException {
        User user = RequestUtil.getUser(req);
        if (user == null)
            throw new CustomException(new Json(JsonMessage.PARAM_INVALID, "用户尚未登录"));

        List<Menu> availableMenus = authService.getVueValuableMenus(user);
        availableMenus = menuService.jsonified(availableMenus);
        return new Json(JsonMessage.SUCCESS, availableMenus);
    }

    /**
     * 根据前端回传的组织id设置用户所属组织
     */
    private void setUserOrganizationByCheckedOrganizations(User user, String organizationId) {
        Organization organization = organizationClientService.getById(organizationId);
        byte level = organization.getLevel();

        Organization bureau = null;
        Organization section = null;
        Organization workshop = null;
        Organization workArea = null;

        switch (level) {
            case Organization.BUREAU -> bureau = organization;
            case Organization.SECTION -> section = organization;
            case Organization.WORKSHOP -> workshop = organization;
            case Organization.WORK_AREA -> workArea = organization;
        }

        if (workArea != null) {
            workshop = organizationClientService.getParent(workArea.getId());
            user.setWorkAreaId(workArea.getId());
        }
        if (workshop != null) {
            section = organizationClientService.getParent(workshop.getId());
            user.setWorkshopId(workshop.getId());
        }
        if (section != null) {
            bureau = organizationClientService.getParent(section.getId());
            user.setSectionId(section.getId());
        }
        if (bureau != null)
            user.setBureauId(bureau.getId());

    }

}
