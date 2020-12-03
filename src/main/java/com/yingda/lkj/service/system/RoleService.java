package com.yingda.lkj.service.system;

import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;

import java.util.List;

/**
 * @author hood  2019/12/18
 */
public interface RoleService {

    List<Role> showDown();

    Role getRole(String id);

    Role getRole(User user);

    void deleteRole(List<String> ids) throws CustomException;

    void saveOrUpdate(Role role);

    Role getRoleByName(String name);

    List<Role> getByOrganizationPermission(byte organizationPermission);

}
