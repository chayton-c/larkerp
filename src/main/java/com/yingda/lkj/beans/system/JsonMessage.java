package com.yingda.lkj.beans.system;

/**
 * @author hood  2019/12/13
 */
public enum JsonMessage {

    // 未处理的
    RAW(false, 0, "未经处理的返回", "网络繁忙，请稍后再试"),

    // 正常返回的枚举
    /**
     * 成功
     */
    SUCCESS(true, 2000, "正常返回", "操作成功"),

    NEED_TO_AUTH(false, 3000, "需要授权", "需要授权"),

    // 系统错误，50开头
    SYS_ERROR(false, 5000, "系统错误", "网络繁忙，请稍后再试"),
    /**
     * 参数错误
     */
    PARAM_INVALID(false, 5001, "参数出现异常", "参数出现异常"),
    /**
     * 数据填写不完整，请检查
     */
    DATA_NO_COMPLETE(false, 5002, "数据填写不完整，请检查", "数据填写不完整，请检查"),
    /**
     * 数据已被删除
     */
    DATE_HAS_BEEN_DELETED(false, 5003, "该数据不存在或已被删除", "该数据不存在或已被删除"),
    /**
     * 没有用户session
     */
    AUTH_ERROR(false, 5004, "授权错误", "您在本页面停留时间过长，请重新进入再试"),
    /**
     * 外部接口错误
     */
    EXTERNAL_INTERFACE_ERROR(false, 5005, "外部接口错误", "网络繁忙，请稍后再试"),
    /**
     * 软件授权错误
     */
    LICENSING_ERROR(false, 5006, "授权错误", "软件授权错误，请联系管理员"),
    /**
     * 外部接口错误
     */
    DUPLICATE_DATA(false, 5007, "数据重复", "输入的数据已存在"),
    /**
     * 外部接口错误
     */
    CONTAINING_ASSOCIATED_DATA(false, 5008, "包含关联数据", "包含关联数据");

    public static void main(String[] args) {
        for (JsonMessage rate : JsonMessage.values())
            System.out.println(rate);
    }

    JsonMessage(boolean success, int code, String msg, String userMsg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.userMsg = userMsg;
    }

    private boolean success;
    private int code;
    private String msg;
    private String userMsg;

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getUserMsg() {
        return userMsg;
    }

    @Override
    public String toString() {
        return "ErrorMsg{" +
                "success=" + success +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", userMsg='" + userMsg + '\'' +
                '}';
    }
}
