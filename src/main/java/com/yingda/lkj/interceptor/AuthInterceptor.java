package com.yingda.lkj.interceptor;

import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.system.Menu;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.service.system.MenuService;
import com.yingda.lkj.service.system.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 页面授权拦截器
 *
 * @author hood  2019/12/18
 */
public class AuthInterceptor implements HandlerInterceptor {

    private MenuService menuService;
    private AuthService authService;

    @Autowired
    public void setMenuService(MenuService menuService) { this.menuService = menuService; }
    @Autowired
    public void setAuthService(AuthService authService) { this.authService = authService; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception  {
        User user = (User) request.getAttribute(Constant.USER_ATTRIBUTE_NAME);
        String uri = request.getRequestURI();

        Menu menu = menuService.getMenuByUrl(uri);

        if (menu == null) // 页面没配权限，不管
            return true;

        if (menu.getHide().equals(Constant.TRUE))
            throw new CustomException(new Json(JsonMessage.AUTH_ERROR, "该页面已被隐藏，请联系管理员"));

        if (authService.hasAccess(user.getRoleId(), menu.getId()))
            return true;

        throw new CustomException(new Json(JsonMessage.AUTH_ERROR, "未获得访问权限，请联系管理员"));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

}
