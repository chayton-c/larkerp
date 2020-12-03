package com.yingda.lkj.controller.system;

import com.yingda.lkj.beans.entity.system.Menu;
import com.yingda.lkj.beans.pojo.system.NgMenu;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.system.MenuService;
import com.yingda.lkj.utils.JsonUtils;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2019/12/25
 */
@Controller
@RequestMapping("/menu")
public class MenuController extends BaseController {

    private MenuService menuService;

    private Menu menu;

    @ModelAttribute
    public void getMenu(Menu menu) {
        this.menu = menu;
    }

    /**
     * 菜单列表
     */
    @RequestMapping("")
    public ModelAndView menu() {
        List<Menu> jsonified = menuService.jsonified(menuService.showDown());
        return new ModelAndView("system/menu", Map.of("menus", jsonified));
    }

    /**
     * 菜单列表
     */
    @RequestMapping("/getNgMenus")
    @ResponseBody
    public Json getNgMenus() {
        List<NgMenu> jsonified = menuService.jsonifiedAsNgMenu(menuService.showDown());
        return new Json(JsonMessage.SUCCESS, Map.of("menus", jsonified));
    }

    @RequestMapping("/updateSeq")
    @ResponseBody
    public Json updateSeq(String id, String seq) {
        Menu menuById = menuService.getMenuById(id);
        menuById.setSeq(Integer.parseInt(seq));
        menuById.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        menuService.saveOrUpdate(menuById);

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/saveOrUpdateMenus")
    @ResponseBody
    public Json saveOrUpdateMenus() {
        String menusStr = req.getParameter("menus");
        List<Menu> menus = JsonUtils.parseList(menusStr, Menu.class);
        for (Menu menu : menus) {
            if (StringUtils.isEmpty(menu.getId()))
                menu.setAddTime(current());

            this.menu.setUpdateTime(current());
            menuService.saveOrUpdate(menu);
        }

        return new Json(JsonMessage.SUCCESS);
    }

    // 后台用的
    @RequestMapping("/saveOrUpdate")
    @ResponseBody
    public Json saveOrUpdate() {
        Timestamp current = new Timestamp(System.currentTimeMillis());

        if (StringUtils.isEmpty(menu.getId())) {
            menu.setId(UUID.randomUUID().toString());
            menu.setAddTime(current);
        }

        menu.setUpdateTime(current);
        menuService.saveOrUpdate(menu);

        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Json delete(String ids) {
        // asList出来的list不能addAll
        menuService.delete(new ArrayList<>(Arrays.asList(ids.split(","))));

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 菜单详情页
     */
    @RequestMapping("/menuInfo")
    public ModelAndView menuInfo(String id, String pid) {
        Menu menuById = menuService.getMenuById(id);

        // 添加的时候(menuId为空)，pid为菜单的上级id，会把这个值放到详情页的表单input中，添加时作为pid(如果为空，那么填写 Menu.ROOT_ID)
        if (StringUtils.isEmpty(id)) {
            if (pid == null) {
                menuById.setLevel(Menu.PRIMARY_MENU);
                menuById.setPid(Menu.ROOT_ID );
            } else {
                menuById.setLevel(Menu.SECONDARY_MENU);
                menuById.setPid(pid);
            }
        }

        return new ModelAndView("/system/menu-info", Map.of("menu", menuById));
    }

    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }
}
