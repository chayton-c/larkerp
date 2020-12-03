package com.yingda.lkj.interceptor;

import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author hood  2020/7/22
 */
public class HeaderInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeaderInterceptor.class);

    private static final List<String> ALLOWED_METHODS = List.of("GET", "POST");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception  {
        String method = request.getMethod();

        // same-origin policy
        String accessControlRequestMethod = request.getHeader("access-control-request-method");
        if (StringUtils.isNotEmpty(accessControlRequestMethod))
            method = accessControlRequestMethod;

        if (!ALLOWED_METHODS.contains(method)) {
            response.sendError(405);
            throw new CustomException(JsonMessage.SYS_ERROR, "405");
        }

        try {
            String domain = request.getRequestURL().toString().replace(request.getRequestURI(), "").split("//")[1];
//            response.setHeader("Content-Security-Policy", "default-src " + domain + " 'self' 'unsafe-inline';");
        } catch (Exception e) {
            LOGGER.error("获取domain异常");
        }
//        response.setHeader("X-XSS-Protection", "1");
//        response.setHeader("X-Frame-Options", "SAMEORIGIN");
//        response.setHeader("X-Content-Type-Options", "nosniff");

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }
}
