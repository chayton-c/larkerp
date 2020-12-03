package com.yingda.lkj.service.system;

import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.RoleOrganization;

import java.util.List;

/**
 * @author hood  2020/11/26
 */
public interface RoleOrganizationService {
    void add(String roleId, String organizationId);
    void delete(String roleId, String organizationId);
    List<RoleOrganization> getByRoleId(String roleId);
    List<RoleOrganization> getByOrganizationId(String organizationId);
}
