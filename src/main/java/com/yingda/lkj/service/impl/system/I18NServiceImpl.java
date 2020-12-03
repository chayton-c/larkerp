package com.yingda.lkj.service.impl.system;

import com.yingda.lkj.beans.entity.system.Menu;
import com.yingda.lkj.service.system.I18NService;
import com.yingda.lkj.service.system.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2020/11/10
 */
@Service("i18NService")
public class I18NServiceImpl implements I18NService {

    @Autowired
    private MenuService menuService;

    private static final Map<String, String> cn = new HashMap<>();

    private void initCN() {
        List<Menu> menus = menuService.showDown();
        for (Menu menu : menus)
            cn.put(menu.getId(), menu.getName());

        cn.put("menu.search.placeholder", "搜索：员工、文件、照片等");
        cn.put("menu.fullscreen", "全屏");
        cn.put("menu.fullscreen.exit", "退出全屏");
        cn.put("menu.clear.local.storage", "清理本地缓存");
        cn.put("menu.lang", "语言");
        cn.put("112358", "主导航");
        cn.put("menu.dashboard", "仪表盘");
        cn.put("menu.dashboard.v1", "默认页");
        cn.put("menu.dashboard.analysis", "分析页");
        cn.put("menu.dashboard.monitor", "监控页");
        cn.put("menu.dashboard.workplace", "工作台");
        cn.put("menu.shortcut", "快捷菜单");
        cn.put("menu.widgets", "小部件");
        cn.put("menu.alain", "Alain");
        cn.put("menu.style", "样式");
        cn.put("menu.style.typography", "字体排印");
        cn.put("menu.style.gridmasonry", "瀑布流");
        cn.put("menu.style.colors", "色彩");
        cn.put("menu.delon", "Delon 类库");
        cn.put("menu.delon.form", "动态表单");
        cn.put("menu.delon.table", "简易表格");
        cn.put("menu.delon.util", "工具集");
        cn.put("menu.delon.print", "打印");
        cn.put("menu.delon.guard", "路由守卫");
        cn.put("menu.delon.cache", "字典缓存");
        cn.put("menu.delon.qr", "二维码");
        cn.put("menu.delon.acl", "基于角色访问控制");
        cn.put("menu.delon.downfile", "下载文件");
        cn.put("menu.delon.xlsx", "Excel操作");
        cn.put("menu.delon.zip", "本地解压缩");
        cn.put("menu.pro", "Antd Pro");
        cn.put("menu.form", "表单页");
        cn.put("menu.form.basicform", "基础表单");
        cn.put("menu.form.stepform", "分步表单");
        cn.put("menu.form.stepform.info", "分步表单（填写转账信息）");
        cn.put("menu.form.stepform.confirm", "分步表单（确认转账信息）");
        cn.put("menu.form.stepform.result", "分步表单（完成）");
        cn.put("menu.form.advancedform", "高级表单");
        cn.put("menu.list", "列表页");
        cn.put("menu.list.searchtable", "查询表格");
        cn.put("menu.list.basiclist", "标准列表");
        cn.put("menu.list.cardlist", "卡片列表");
        cn.put("menu.list.searchlist", "搜索列表");
        cn.put("menu.list.searchlist.articles", "搜索列表（文章）");
        cn.put("menu.list.searchlist.projects", "搜索列表（项目）");
        cn.put("menu.list.searchlist.applications", "搜索列表（应用）");
        cn.put("menu.profile", "详情页");
        cn.put("menu.profile.basic", "基础详情页");
        cn.put("menu.profile.advanced", "高级详情页");
        cn.put("menu.result", "结果页");
        cn.put("menu.result.success", "成功页");
        cn.put("menu.result.fail", "失败页");
        cn.put("menu.exception", "异常页");
        cn.put("menu.exception.not-permission", "403");
        cn.put("menu.exception.not-find", "404");
        cn.put("menu.exception.server-error", "500");
        cn.put("menu.account", "个人页");
        cn.put("menu.account.center", "个人中心");
        cn.put("menu.account.settings", "个人设置");
        cn.put("menu.account.trigger", "触发错误");
        cn.put("menu.account.logout", "退出登录");
        cn.put("menu.more", "更多");
        cn.put("menu.report", "报表");
        cn.put("menu.report.relation", "全屏关系图");
        cn.put("menu.extras", "扩展");
        cn.put("menu.extras.helpcenter", "帮助中心");
        cn.put("menu.extras.settings", "设置");
        cn.put("menu.extras.poi", "门店");
        cn.put("app.analysis.test", "工专路 {{no}} 号店");
        cn.put("app.analysis.introduce", "指标说明");
        cn.put("app.analysis.total-sales", "总销售额");
        cn.put("app.analysis.day-sales", "日销售额");
        cn.put("app.analysis.visits", "访问量");
        cn.put("app.analysis.visits-trend", "访问量趋势");
        cn.put("app.analysis.visits-ranking", "门店访问量排名");
        cn.put("app.analysis.day-visits", "日访问量");
        cn.put("app.analysis.week", "周同比");
        cn.put("app.analysis.day", "日同比");
        cn.put("app.analysis.payments", "支付笔数");
        cn.put("app.analysis.conversion-rate", "转化率");
        cn.put("app.analysis.operational-effect", "运营活动效果");
        cn.put("app.analysis.sales-trend", "销售趋势");
        cn.put("app.analysis.sales-ranking", "门店销售额排名");
        cn.put("app.analysis.all-year", "全年");
        cn.put("app.analysis.all-month", "本月");
        cn.put("app.analysis.all-week", "本周");
        cn.put("app.analysis.all-day", "今日");
        cn.put("app.analysis.search-users", "搜索用户数");
        cn.put("app.analysis.per-capita-search", "人均搜索次数");
        cn.put("app.analysis.online-top-search", "线上热门搜索");
        cn.put("app.analysis.the-proportion-of-sales", "销售额类别占比");
        cn.put("app.analysis.channel.all", "全部渠道");
        cn.put("app.analysis.channel.online", "线上");
        cn.put("app.analysis.channel.stores", "门店");
        cn.put("app.analysis.sales", "销售额");
        cn.put("app.analysis.traffic", "客流量");
        cn.put("app.analysis.table.rank", "排名");
        cn.put("app.analysis.table.search-keyword", "搜索关键词");
        cn.put("app.analysis.table.users", "用户数");
        cn.put("app.analysis.table.weekly-range", "周涨幅");
        cn.put("app.monitor.trading-activity", "活动实时交易情况");
        cn.put("app.monitor.total-transactions", "今日交易总额");
        cn.put("app.monitor.sales-target", "销售目标完成率");
        cn.put("app.monitor.remaining-time", "活动剩余时间");
        cn.put("app.monitor.total-transactions-per-second", "每秒交易总额");
        cn.put("app.monitor.activity-forecast", "活动情况预测");
        cn.put("app.monitor.efficiency", "券核效率");
        cn.put("app.monitor.ratio", "跳出率");
        cn.put("app.monitor.proportion-per-category", "各品类占比");
        cn.put("app.monitor.fast-food", "中式快餐");
        cn.put("app.monitor.western-food", "西餐");
        cn.put("app.monitor.hot-pot", "火锅");
        cn.put("app.monitor.waiting-for-implementation", "等待后期实现");
        cn.put("app.monitor.popular-searches", "热门搜索");
        cn.put("app.monitor.resource-surplus", "资源剩余");
        cn.put("app.monitor.fund-surplus", "补贴资金剩余");
        cn.put("app.lock", "锁屏");
        cn.put("app.login.message-invalid-credentials", "账户或密码错误（admin/ant.design）");
        cn.put("app.login.message-invalid-verification-code", "验证码错误");
        cn.put("app.login.tab-login-credentials", "账户密码登录");
        cn.put("app.login.tab-login-mobile", "手机号登录");
        cn.put("app.login.remember-me", "自动登录");
        cn.put("app.login.forgot-password", "忘记密码");
        cn.put("app.login.sign-in-with", "其他登录方式");
        cn.put("app.login.signup", "注册账户");
        cn.put("app.login.login", "登录");
        cn.put("app.register.register", "注册");
        cn.put("app.register.get-verification-code", "获取验证码");
        cn.put("app.register.sign-in", "使用已有账户登录");
        cn.put("app.register-result.msg", "你的账户：{{email}} 注册成功");
        cn.put("app.register-result.activation-email", "激活邮件已发送到你的邮箱中，邮件有效期为24小时。请及时登录邮箱，点击邮件中的链接激活帐户。");
        cn.put("app.register-result.back-home", "返回首页");
        cn.put("app.register-result.view-mailbox", "查看邮箱");
        cn.put("validation.email.required", "请输入邮箱地址！");
        cn.put("validation.email.wrong-format", "邮箱地址格式错误！");
        cn.put("validation.password.required", "请输入密码！");
        cn.put("validation.password.twice", "两次输入的密码不匹配!");
        cn.put("validation.password.strength.msg", "请至少输入 6 个字符。请不要使用容易被猜到的密码。");
        cn.put("validation.password.strength.strong", "强度：强");
        cn.put("validation.password.strength.medium", "强度：中");
        cn.put("validation.password.strength.short", "强度：太短");
        cn.put("validation.confirm-password.required", "请确认密码！");
        cn.put("validation.phone-number.required", "请输入手机号！");
        cn.put("validation.phone-number.wrong-format", "手机号格式错误！");
        cn.put("validation.verification-code.required", "请输入验证码！");
        cn.put("validation.title.required", "请输入标题");
        cn.put("validation.date.required", "请选择起止日期");
        cn.put("validation.goal.required", "请输入目标描述");
        cn.put("validation.standard.required", "请输入衡量标准");
    }

    @Override
    public Map<String, String> getCN() {
        if (cn.isEmpty())
            initCN();

        return cn;
    }
}
