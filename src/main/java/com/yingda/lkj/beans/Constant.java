package com.yingda.lkj.beans;

/**
 * @author hood  2019/12/18
 */
public class Constant {


    // hide 字段的 显示 / 隐藏
    public static final Byte SHOW = 0;
    public static final Byte HIDE = 1;

    public static final Byte FALSE = 0;
    public static final Byte TRUE = 1;

    public static final String FALSE_STR = "0";
    public static final String TRUE_STR = "1";

    public static boolean DEBUG = false;

    /**
     * <p>token在请求的header(也可能放到post里？)中的key的名字</p>
     */
    public static final String TOKEN_ATTRIBUTE_NAME = "token";

    /**
     * <p>在AuthInterceptor.preHandle中获取到User后，</p>
     * <p>会暂时性的把User 放到 request中: </p>
     * <p>  request.setAttribute(USER_ATTRIBUTE_NAME, user)</p>
     * <p>便于controller获取到登录的用户</p>
     *
     * <p>然后在AuthInterceptor.postHandle中，为了安全着想</p>
     * <p>会把这个User remove掉 </p>
     * <p>  request.setAttribute(USER_ATTRIBUTE_NAME, user)</p>
     *
     */
    public static final String USER_ATTRIBUTE_NAME = "user";

    // 项目名称，校验证书和用户token时会使用projectName区分不同项目
    public static String projectName;
}
