package com.yingda.lkj.controller.system;

import com.yingda.lkj.beans.entity.system.Menu;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 *
 * @author hood  2020/3/3
 */
@Controller
@RequestMapping("/gongshenhuoerdi")
public class MenuInitController {
    @Autowired
    private BaseService<Menu> menuBaseService;

    private String mainMenus = "主页,基本元素,组件页面,排版布局,订单管理,管理员管理,用户管理,系统统计,组件页面";
    private String sec = "控制台,\n" +
            "图标字体,表单元素,表单组合,按钮,导航/面包屑,选项卡,进度条,面板,微章,时间线,静态表格,动画,\n" +
            "文件上传,分页,多级分类,轮播图,城市三级联动,\n" +
            "栅格,排版,\n" +
            "订单列表,\n" +
            "管理员列表,角色管理,权限分类,菜单管理,\n" +
            "会员列表,会员删除,\n" +
            "拆线图,柱状图,地图,饼图,雷达图,k线图,热力图,仪表图,\n" +
            "文件上传,分页,多级分类,轮播图,城市三级联动";

    private String secUrl = "/html/welcome,\n" +
            "/html/unicode,html/form1,html/form2,html/buttons,html/nav,html/tab,html/progressBar,html/panel,html/badge,html/timeline,html/tableElement," +
            "html/anim,\n" +
            "html/upload,html/page,html/cate,html/carousel,html/city,\n" +
            "html/grid,html/welcome2,\n" +
            "html/orderList,\n" +
            "html/adminList,/role,html/adminCate,/menu,\n" +
            "html/memberList,html/memberDel,\n" +
            "html/echarts1,html/echarts2,html/echarts3,html/echarts4,html/echarts5,html/echarts6,html/echarts7,html/echarts8,\n" +
            "html/upload,html/page,html/cate,html/carousel,html/city";

    @RequestMapping("test111")
    @ResponseBody
    public Json testaaaa() {
        List<Menu> akagi = new ArrayList<>();
        for (int i = 0; i < mainMenus.length(); i++) {
            String mainId = UUID.randomUUID().toString();
            String mainName = mainMenus.split(",")[i];
            Menu menu = new Menu(mainId, Menu.ROOT_ID, "-1", mainName, i, Menu.PRIMARY_MENU);
            akagi.add(menu);

            String[] didiNames = sec.split("\\n")[i].split(",");
            String[] didiUrls = secUrl.split("\\n")[i].split(",");

            for (int j = 0; j < didiNames.length; j++) {
                String didiName = didiNames[j];
                String didiUrl = didiUrls[j];
                Menu menu1 = new Menu(UUID.randomUUID().toString(), menu.getId(), didiUrl, didiName, j, Menu.SECONDARY_MENU);
                akagi.add(menu1);
            }
        }

        menuBaseService.bulkInsert(akagi);

        return new Json(JsonMessage.SUCCESS);
    }
}
