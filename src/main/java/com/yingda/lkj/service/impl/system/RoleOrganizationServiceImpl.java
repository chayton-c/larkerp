package com.yingda.lkj.service.impl.system;

import com.yingda.lkj.beans.entity.system.RoleOrganization;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.system.RoleOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/11/26
 */
@Service("roleOrganizationServiceImpl")
public class RoleOrganizationServiceImpl implements RoleOrganizationService {

    @Autowired
    private BaseDao<RoleOrganization> roleOrganizationBaseDao;

    @Override
    public void add(String roleId, String organizationId) {
        RoleOrganization roleOrganization = get(roleId, organizationId);
        if (roleOrganization == null)
            roleOrganizationBaseDao.saveOrUpdate(new RoleOrganization(roleId, organizationId));
    }

    @Override
    public void delete(String roleId, String organizationId) {
        roleOrganizationBaseDao.executeHql(
                "delete from RoleOrganization where roleId = :roleId and organizationId = :organizationId",
                Map.of("roleId", roleId, "organizationId", organizationId)
        );
    }

    @Override
    public List<RoleOrganization> getByRoleId(String roleId) {
        return roleOrganizationBaseDao.find(
                "from RoleOrganization where roleId = :roleId",
                Map.of("roleId", roleId)
        );
    }

    @Override
    public List<RoleOrganization> getByOrganizationId(String organizationId) {
        return roleOrganizationBaseDao.find(
                "from RoleOrganization where organizationId = :organizationId",
                Map.of("organizationId", organizationId)
        );
    }

    private RoleOrganization get(String roleId, String organizationId) {
        return roleOrganizationBaseDao.get(
                "from RoleOrganization where roleId = :roleId and organizationId = :organizationId",
                Map.of("roleId", roleId, "organizationId", organizationId)
        );
    }
}
