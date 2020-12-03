package com.yingda.lkj.service.system;

import com.yingda.lkj.beans.entity.system.Menu;
import com.yingda.lkj.beans.pojo.system.NgMenu;

import java.util.List;

/**
 * <span>菜单操作</span>
 * <span>注意：这里默认只有两级菜单</span>
 *
 * @author hood  2019/12/18
 */
public interface MenuService {

    // getAllObjects
    List<Menu> showDown();

    Menu getMenuByUrl(String url);

    Menu getMenuById(String id);

    /**
     * 获取指定菜单中上级为parentId的所有下级菜单
     */
    List<Menu> getSlave(String parentId, List<Menu> valuableMenus);

    void saveOrUpdate(Menu menu);

    void delete(List<String> ids);

    /**
     * 把一组menu通过pid转为menuTree,默认只有三级
     */
    List<Menu> jsonified(List<Menu> menus);
//
//    List<Menu> jsonifiedByName(List<Menu> menus, String name);

    List<NgMenu> jsonifiedAsNgMenu(List<Menu> menus);
}
