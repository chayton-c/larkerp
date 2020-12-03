package com.yingda.lkj.controller.system;

import com.yingda.lkj.beans.entity.backstage.device.Device;
import com.yingda.lkj.beans.entity.backstage.measurement.MeasurementTaskDetail;
import com.yingda.lkj.beans.entity.system.Menu;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.DeviceStatistics;
import com.yingda.lkj.beans.pojo.lkj.lkjtask.UserLkjTask;
import com.yingda.lkj.beans.pojo.measurement.MeasurementTaskStatistics;
import com.yingda.lkj.beans.pojo.measurement.UserMeasurementTaskDetail;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.device.DeviceService;
import com.yingda.lkj.service.backstage.lkjtask.LkjTaskService;
import com.yingda.lkj.service.backstage.measurement.MeasurementTaskService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.service.system.AuthService;
import com.yingda.lkj.service.system.MenuService;
import com.yingda.lkj.utils.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * @author hood  2019/12/18
 */
@Controller
public class IndexController extends BaseController {

    @Autowired
    private AuthService authService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private BaseService<Menu> menuBaseService;
    @Autowired
    private LkjTaskService lkjTaskService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private BaseService<MeasurementTaskDetail> measurementTaskDetailBaseService;
    @Autowired
    private MeasurementTaskService measurementTaskService;

    @RequestMapping("/")
    public ModelAndView login() throws CustomException {
        return index();
    }

    @RequestMapping("/index")
    public ModelAndView index() throws CustomException {
        Map<String, Object> attributes = new HashMap<>();

        User user = getUser();
        List<Menu> valuableMenus = authService.getValuableMenus(user);
        // 一级菜单
        List<Menu> availableMenus = menuService.jsonified(valuableMenus);
        // 子菜单
        List<Menu> slaves = menuService.getSlave(req.getParameter("parentId"), valuableMenus);

        attributes.put("menus", availableMenus);
        attributes.put("slaves", slaves);
        attributes.put("user", user);

        return new ModelAndView("index", attributes);
    }

    @RequestMapping("/welcome")
    public ModelAndView welcome() {
        User user = RequestUtil.getUser(req);
        String userId = user.getId();
        UserLkjTask userLkjTask = lkjTaskService.userTaskInfo(userId);
        UserMeasurementTaskDetail userMeasurementTaskDetail = measurementTaskService.getUserMeasurementTaskDetail(userId);

        return new ModelAndView(
                "/welcome",
                Map.of("user", user, "userLkjTask", userLkjTask, "userMeasurementTaskDetail", userMeasurementTaskDetail)
        );
    }

    @RequestMapping("/indexCharts")
    @ResponseBody
    public Json indexCharts() throws Exception {
//        String startTimeStr = req.getParameter("startTimeStr");
//        String endTimeStr = req.getParameter("endTimeStr");

        List<Device> devices = deviceService.getBySectionId(getSectionId());
        DeviceStatistics deviceStatistics = new DeviceStatistics(devices);

//        List<MeasurementTaskDetail> measurementTaskDetails = measurementTaskDetailBaseService.find(
//                "from MeasurementTaskDetail where addTime > :startTime and addTime < :endTime",
//                Map.of("startTime", DateUtil.toTimestamp(startTimeStr), "endTime", DateUtil.toTimestamp(endTimeStr))
//        );
        List<MeasurementTaskDetail> measurementTaskDetails = measurementTaskDetailBaseService.find(
                "from MeasurementTaskDetail"
        );
        MeasurementTaskStatistics measurementTaskStatistics = new MeasurementTaskStatistics(measurementTaskDetails);

        return new Json(JsonMessage.SUCCESS, Map.of("deviceStatistics", deviceStatistics, "measurementTaskStatistics", measurementTaskStatistics));
    }

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
        for (int i = 0; i < 9; i++) {
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
