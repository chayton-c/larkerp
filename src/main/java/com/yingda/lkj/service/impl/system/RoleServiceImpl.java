package com.yingda.lkj.service.impl.system;

import com.yingda.lkj.annotation.CacheMethod;
import com.yingda.lkj.beans.entity.system.Role;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.beans.system.cache.CacheMap;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.system.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2019/12/18
 */
@CacheMethod
@Service("roleService")
public class RoleServiceImpl implements RoleService {

    private static final Map<String, Role> ROLE_MAP = new CacheMap<>();

    @Autowired
    private BaseDao<Role> roleBaseDao;
    @Autowired
    private BaseDao<User> userBaseDao;


    @Override
    public List<Role> showDown() {
        init();
        return new ArrayList<>(ROLE_MAP.values());
    }

    @Override
    public Role getRole(String id) {
        init();
        return Optional.ofNullable(id).map(ROLE_MAP::get).orElseGet(Role::new);
    }

    @Override
    public Role getRole(User user) {
        String roleId = user.getRoleId();
        return getRole(roleId);
    }

    @Override
    public void deleteRole(List<String> ids) throws CustomException {
        List<User> users = userBaseDao.find("from User where roleId in :ids", Map.of("ids", ids));
        if (!users.isEmpty())
            throw new CustomException(JsonMessage.CONTAINING_ASSOCIATED_DATA, "仍有用户使用指定岗位，请在修改对应角色的岗位后再尝试删除");

        for (String id : ids) {
            Role role = ROLE_MAP.get(id);
            ROLE_MAP.remove(id);
            if (role != null)
                roleBaseDao.delete(role);
        }
    }

    @Override
    public void saveOrUpdate(Role role) {
        init();
        roleBaseDao.saveOrUpdate(role);
        roleBaseDao.flush();
        ROLE_MAP.put(role.getId(), role);
    }

    @Override
    public Role getRoleByName(String name) {
        init();

        List<Role> roles = showDown().stream().filter(x -> name.equals(x.getName())).collect(Collectors.toList());
        if (roles.isEmpty())
            return null;
        return roles.get(0);
    }

    @Override
    public List<Role> getByOrganizationPermission(byte organizationPermission) {
        init();
        return showDown().stream().filter(x -> x.getOrganizationPermission() == organizationPermission).collect(Collectors.toList());
    }

    private void init() {
        if (!ROLE_MAP.isEmpty())
            return;

        List<Role> roles = roleBaseDao.find("from Role");

        ROLE_MAP.putAll(roles.stream().collect(Collectors.toMap(Role::getId, role -> role)));
    }
}
