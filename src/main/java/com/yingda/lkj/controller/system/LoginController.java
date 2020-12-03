package com.yingda.lkj.controller.system;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.system.UserService;
import com.yingda.lkj.utils.LicensingUtil;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hood  2019/12/16
 */
@Controller
@RequestMapping("/auth")
public class LoginController extends BaseController {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    // TODO 密码未加密
    @RequestMapping(value = "/login")
    @ResponseBody
    public Json login(String username, String password) {
        Map<String, Object> attributes = new HashMap<>();

        User user = userService.getUserByUserName(username);
        if (user == null)
            return new Json(JsonMessage.AUTH_ERROR, "找不到该用户");

        String realPassword = user.getPassword();
        if (!realPassword.equals(password))
            return new Json(JsonMessage.AUTH_ERROR, "密码错误");

        if (!Constant.DEBUG) {
            Json validate = LicensingUtil.validate();
            if (!validate.isSuccess()) {
                userService.login(user);
                return validate;
            }

//         验证是否修改了系统时间到证书验证时间前
            Timestamp loginTime = user.getLoginTime();
            long certValidateTime = (long) validate.getObj();
            if (loginTime != null && certValidateTime <= loginTime.getTime())
                return new Json(JsonMessage.LICENSING_ERROR, "检测到您修改了服务器系统时间，请联系管理员后重试");
            userService.login(user);
        }

        String token = JWTUtil.createToken(username);
        user.setToken(token);
        attributes.put("user", user);

        return new Json(JsonMessage.SUCCESS, attributes);
    }

    @RequestMapping("/refresh")
    @ResponseBody
    public Json refresh() throws CustomException {
        return new Json(JsonMessage.SUCCESS, Map.of("token", RequestUtil.getToken(req)), "ok");
    }
}
