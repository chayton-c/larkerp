package com.yingda.lkj.config;

import com.yingda.lkj.interceptor.AuthInterceptor;
import com.yingda.lkj.interceptor.CommonInterceptor;
import com.yingda.lkj.interceptor.HeaderInterceptor;
import com.yingda.lkj.interceptor.TokenInterceptor;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author hood  2019/12/18
 */
@Configuration
public class MVCConfigurer implements WebMvcConfigurer {
    /**
     * 添加自定义的Converters和Formatters.
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new TimestampConverter());
    }

    @Bean
    public TokenInterceptor getTokenInterceptor() {
        return new TokenInterceptor();
    }
    @Bean
    public AuthInterceptor getAuthInterceptor() {
        return new AuthInterceptor();
    }
    @Bean
    public CommonInterceptor getCommonInterceptor() { return new CommonInterceptor(); }
    @Bean
    public HeaderInterceptor getHeaderInterceptor() { return new HeaderInterceptor(); }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // header拦截器
        registry.addInterceptor(getHeaderInterceptor()).addPathPatterns("/**");
        // 通用拦截器
        registry.addInterceptor(getCommonInterceptor()).excludePathPatterns(commonInterceptorExcludes).addPathPatterns("/**");
        // token解析拦截器
        registry.addInterceptor(getTokenInterceptor()).excludePathPatterns(authInterceptorExcludes).addPathPatterns("/**");
        // 权限拦截器
        registry.addInterceptor(getAuthInterceptor()).excludePathPatterns(authInterceptorExcludes).addPathPatterns("/**");
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error/404");
            factory.addErrorPages(errorPage404, errorPage404);
        };
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowCredentials(true)
                        .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH", "DELETE")
                        .maxAge(3600);
            }
        };
    }

    private final List<String> authInterceptorExcludes = List.of(
            "/role/**", // 基础信息
            "/menu/**", // 基础信息
            "/chart/**", // 基础信息
            "/init/**", // 基础信息
            "/auth/**", // 授权
            "/assets/**", // 静态资源
            "/error/**", // 错误页
            "/css/**", // 静态资源
            "/uploadimg/app/**", // app下载
            "/app/**",
            "/fonts/**",
            "/images/**",
            "/js/**",
            "/lib/**",
            "/test/**",
            "/favicon.ico",
            "/**/test_**" // 测试方法
    );

    private final List<String> commonInterceptorExcludes = List.of(
            "/css/**", // 静态资源
            "/assets/**", // 静态资源
            "/baiduMapOfflineTiles/**", // 百度地图
            "/error/**", // 错误页
            "/images/**",
            "/js/**",
            "/lib/**",
            "/favicon.ico"
    );
}
