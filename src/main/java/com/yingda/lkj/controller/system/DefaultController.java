package com.yingda.lkj.controller.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 功能演示
 *
 * @author hood  2019/12/18
 */
@Controller
@RequestMapping("/html")
public class DefaultController {
    @RequestMapping("/adminAdd")
    public String adminAdd() {
        return "/html/admin-add";
    }
    @RequestMapping("/adminCate")
    public String adminCate() {
        return "/html/admin-cate";
    }
    @RequestMapping("/adminEdit")
    public String adminEdit() {
        return "/html/admin-edit";
    }
    @RequestMapping("/adminList")
    public String adminList() {
        return "/html/admin-list";
    }
    @RequestMapping("/adminRule")
    public String adminRule() {
        return "/html/admin-rule";
    }
    @RequestMapping("/anim")
    public String anim() {
        return "/html/anim";
    }
    @RequestMapping("/auxiliar")
    public String auxiliar() {
        return "/html/auxiliar";
    }
    @RequestMapping("/badge")
    public String badge() {
        return "/html/badge";
    }
    @RequestMapping("/buttons")
    public String buttons() {
        return "/html/buttons";
    }
    @RequestMapping("/carousel")
    public String carousel() {
        return "/html/carousel";
    }
    @RequestMapping("/cate")
    public String cate() {
        return "/html/cate";
    }
    @RequestMapping("/city")
    public String city() {
        return "/html/city";
    }
    @RequestMapping("/colorpicker")
    public String colorpicker() {
        return "/html/colorpicker";
    }
    @RequestMapping("/echarts1")
    public String echarts1() {
        return "/html/echarts1";
    }
    @RequestMapping("/echarts2")
    public String echarts2() {
        return "/html/echarts2";
    }
    @RequestMapping("/echarts3")
    public String echarts3() {
        return "/html/echarts3";
    }
    @RequestMapping("/echarts4")
    public String echarts4() {
        return "/html/echarts4";
    }
    @RequestMapping("/echarts5")
    public String echarts5() {
        return "/html/echarts5";
    }
    @RequestMapping("/echarts6")
    public String echarts6() {
        return "/html/echarts6";
    }
    @RequestMapping("/echarts7")
    public String echarts7() {
        return "/html/echarts7";
    }
    @RequestMapping("/echarts8")
    public String echarts8() {
        return "/html/echarts8";
    }
    @RequestMapping("/form1")
    public String form1() {
        return "/html/form1";
    }
    @RequestMapping("/form2")
    public String form2() {
        return "/html/form2";
    }
    @RequestMapping("/grid")
    public String grid() {
        return "/html/grid";
    }
    @RequestMapping("/memberAdd")
    public String memberAdd() {
        return "/html/member-add";
    }
    @RequestMapping("/memberDel")
    public String memberDel() {
        return "/html/member-del";
    }
    @RequestMapping("/memberEdit")
    public String memberEdit() {
        return "/html/member-edit";
    }
    @RequestMapping("/memberList")
    public String memberList() {
        return "/html/member-list";
    }
    @RequestMapping("/memberPassword")
    public String memberPassword() {
        return "/html/member-password";
    }
    @RequestMapping("/nav")
    public String nav() {
        return "/html/nav";
    }
    @RequestMapping("/orderAdd")
    public String orderAdd() {
        return "/html/order-add";
    }
    @RequestMapping("/orderList")
    public String orderList() {
        return "/html/order-list";
    }
    @RequestMapping("/page")
    public String page() {
        return "/html/page";
    }
    @RequestMapping("/panel")
    public String panel() {
        return "/html/panel";
    }
    @RequestMapping("/progressBar")
    public String progressBar() {
        return "/html/progress-bar";
    }
    @RequestMapping("/roleAdd")
    public String roleAdd() {
        return "/html/role-add";
    }
    @RequestMapping("/tab")
    public String tab() {
        return "/html/tab";
    }
    @RequestMapping("/tableElement")
    public String tableElement() {
        return "/html/table-element";
    }
    @RequestMapping("/timeline")
    public String timeline() {
        return "/html/timeline";
    }
    @RequestMapping("/unicode")
    public String unicode() {
        return "/html/unicode";
    }
    @RequestMapping("/upload")
    public String upload() {
        return "/html/upload";
    }
    @RequestMapping("/welcome2")
    public String welcome2() {
        return "/html/welcome2";
    }
}
