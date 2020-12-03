package com.yingda.lkj.service.impl.system;

import com.yingda.lkj.annotation.CacheMethod;
import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.system.Menu;
import com.yingda.lkj.beans.entity.system.RoleMenu;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.system.cache.CacheMap;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.system.AuthService;
import com.yingda.lkj.service.system.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2019/12/18
 */
@CacheMethod
@Service("authService")
public class AuthServiceImpl implements AuthService {

    private BaseDao<RoleMenu> roleMenuBaseDao;
    private MenuService menuService;

    // key为 roleId + menuId 注意不是id
    private static final Map<String, RoleMenu> ROLE_MENU_MAP = new CacheMap<>();

    @Override
    public void addAuth(String roleId, String menuId) {
        init();
        String key = roleId + menuId;
        RoleMenu roleMenu = ROLE_MENU_MAP.get(key);
        if (roleMenu != null)
            return;

        roleMenu = new RoleMenu(roleId, menuId);
        roleMenuBaseDao.saveOrUpdate(roleMenu);
        ROLE_MENU_MAP.put(key, roleMenu);
    }

    @Override
    public void updateAuthBackstage(String roleId, List<String> menuIds) {
        init();
        List<String> templateMenuIds = menuService.showDown().stream().filter(x -> x.getType() == Menu.THYMELEAF).map(Menu::getId).collect(Collectors.toList());
        // 先删除roleId下的所有roleMenu
        List<RoleMenu> roleMenus = roleMenuBaseDao.find(
                "from RoleMenu where roleId = :roleId and menuId in :templateMenuIds",
                Map.of("roleId", roleId, "templateMenuIds", templateMenuIds)
        );
        roleMenus.forEach(this::deleteRoleMenu);

        // 再添加menuIds对应的
        for (String menuId : menuIds)
            saveOrUpdate(new RoleMenu(roleId, menuId));
    }

    private void saveOrUpdate(RoleMenu roleMenu) {
        init();
        roleMenuBaseDao.saveOrUpdate(roleMenu);
        ROLE_MENU_MAP.put(roleMenu.getRoleId() + roleMenu.getMenuId(), roleMenu);
    }

    private void deleteRoleMenu(RoleMenu roleMenu) {
        init();
        roleMenuBaseDao.delete(roleMenu);
        ROLE_MENU_MAP.remove(roleMenu.getRoleId() + roleMenu.getMenuId());
    }

    @Override
    public void removeAuth(String roleId, String menuId) {
        init();
        String key = roleId + menuId;
        RoleMenu roleMenu = ROLE_MENU_MAP.get(key);
        if (roleMenu == null)
            return;

        roleMenuBaseDao.delete(roleMenu);
        ROLE_MENU_MAP.remove(key);
    }

    @Override
    public boolean hasAccess(String roleId, String menuId) {
        init();
        return ROLE_MENU_MAP.get(roleId + menuId) != null;
    }

    @Override
    public List<Menu> getValuableMenus(User user) {
        init();
        return menuService
                .showDown()
                .stream()
                .filter(x -> x.getType() == Menu.THYMELEAF)
                .filter(x -> hasAccess(user.getRoleId(), x.getId()))
                .filter(x -> x.getHide().equals(Constant.SHOW))
                .collect(Collectors.toList());
    }

    @Override
    public List<Menu> getVueValuableMenus(User user) {
        return menuService
                .showDown()
                .stream()
                .filter(x -> x.getType() == Menu.VUE)
                .filter(x -> hasAccess(user.getRoleId(), x.getId()))
                .filter(x -> x.getHide().equals(Constant.SHOW) || user.getUserName().equals("huoerdi")) // 霍尔蒂什么都给看
                .collect(Collectors.toList());
    }

    private void init() {
        if (!ROLE_MENU_MAP.isEmpty())
            return;
        List<RoleMenu> roleMenus = roleMenuBaseDao.find("from RoleMenu");
        ROLE_MENU_MAP.putAll(roleMenus.stream().collect(Collectors.toMap(x -> x.getRoleId() + x.getMenuId(), x -> x)));
    }

    @Autowired
    public void setRoleMenuBaseDao(BaseDao<RoleMenu> roleMenuBaseDao) {
        this.roleMenuBaseDao = roleMenuBaseDao;
    }

    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }
}
