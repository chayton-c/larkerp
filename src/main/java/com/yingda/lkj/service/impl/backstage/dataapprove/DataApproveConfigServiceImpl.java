package com.yingda.lkj.service.impl.backstage.dataapprove;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataApproveConfig;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.dataapprove.DataApproveConfigService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.system.RoleService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2020/5/27
 */
@Service("dataApproveConfigService")
public class DataApproveConfigServiceImpl implements DataApproveConfigService {
    @Autowired
    private BaseDao<DataApproveConfig> dataApproveConfigBaseDao;
    @Autowired
    private BaseDao<User> userBaseDao;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private RoleService roleService;

    @Override
    public List<User> getAvailableApproveUsers(User user) throws Exception {
        String sectionId = user.getSectionId();
        String roleId = user.getRoleId();

        // 查询站段下的审批配置
        List<DataApproveConfig> dataApproveConfigList = getDataApproveConfigList(sectionId);

        // key: 左节点， value：右节点
        Map<String, String> nodeMap = dataApproveConfigList.stream().collect(Collectors.toMap(DataApproveConfig::getLeftNodeRoleId,
                DataApproveConfig::getRightNodeRoleId));

        if (!nodeMap.containsKey(roleId) && !nodeMap.containsValue(roleId)) // 左右节点里都没有这个角色，抛异常
            throw new CustomException(new Json(JsonMessage.SYS_ERROR, "用户: " + user.getDisplayName() + " 在站段："
                    + organizationClientService.getById(sectionId).getName() + " 中无审批部分权限，请联系管理员检查菜单配置"));


        String rightNodeRoleId = nodeMap.get(roleId);
        if (rightNodeRoleId == null) // 审批节点最后一级
            return new ArrayList<>();


        List<User> availableUsers = userBaseDao.find("from User where roleId = :roleId and sectionId = :sectionId",
                Map.of("roleId", rightNodeRoleId, "sectionId", sectionId));

        // 有下级角色，但是站段里没有这个角色的人
        if (availableUsers.isEmpty()) {
            Role nonUserRole = roleService.getRole(rightNodeRoleId);
            Organization section = organizationClientService.getById(sectionId);
            throw new CustomException(new Json(JsonMessage.SYS_ERROR, "站段: " + section.getName() + " 中没有角色为：" + nonUserRole.getName()
                    + " 的用户，请联系管理员检查菜单配置"));
        }

        return availableUsers;
    }

    @Override
    public void saveDataApproveList(List<DataApproveConfig> dataApproveConfigs) {
        List<String> sectionIds = dataApproveConfigs.stream().map(DataApproveConfig::getSectionId).collect(Collectors.toList());
        dataApproveConfigBaseDao.executeHql("delete from DataApproveConfig where sectionId in :sectionIds", Map.of("sectionIds", sectionIds));
        dataApproveConfigBaseDao.bulkInsert(dataApproveConfigs);
    }

    @Override
    public List<DataApproveConfig> getDataApproveConfigList(String sectionId) {
        return dataApproveConfigBaseDao.find("from DataApproveConfig where sectionId = :sectionId", Map.of("sectionId", sectionId));
    }


    @Override
    public String format(List<DataApproveConfig> dataApproveConfigs) {
        StringBuilder result = new StringBuilder();
        for (DataApproveConfig lkjApproveConfig : dataApproveConfigs) {
            Role roleLeft = roleService.getRole(lkjApproveConfig.getLeftNodeRoleId());
            Role roleRight = roleService.getRole(lkjApproveConfig.getRightNodeRoleId());
            String roleLeftStr = roleLeft.getName();
            String roleRightStr = roleRight.getName();

            result.append(roleLeftStr).append(", ").append(roleRightStr).append("; ");
        }
        return result.toString();
    }

    @Override
    public List<DataApproveConfig> parse(String dataApproveConfigListStr, String sectionId) throws CustomException {
        List<DataApproveConfig> resultList = new ArrayList<>();

        String[] lkjApproveConfigListArr = dataApproveConfigListStr.split(";");
        for (String dataApproveConfigStr : lkjApproveConfigListArr) {
            if (StringUtils.isEmpty(dataApproveConfigStr.trim()))
                continue;

            String[] roleNames = dataApproveConfigStr.trim().split(",");
            String leftRoleName = roleNames[0].trim();
            String rightRoleName = roleNames[1].trim();

            Role leftRole = roleService.getRoleByName(leftRoleName);
            if (StringUtils.isNotEmpty(leftRole))
                throw new CustomException(new Json(JsonMessage.PARAM_INVALID, String.format("找不到名称为%s的角色", leftRoleName)));
            Role rightRole = roleService.getRoleByName(rightRoleName);
            if (StringUtils.isNotEmpty(rightRoleName))
                throw new CustomException(new Json(JsonMessage.PARAM_INVALID, String.format("找不到名称为%s的角色", rightRoleName)));

            resultList.add(new DataApproveConfig(sectionId, leftRole.getId(), rightRole.getId()));
        }
        return resultList;
    }
}
