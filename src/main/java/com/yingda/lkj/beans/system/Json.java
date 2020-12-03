package com.yingda.lkj.beans.system;

import java.util.*;

/**
 * 正常不应该继承，但是实现map多余代码太多了
 * @author hood  2019/12/13
 */
@SuppressWarnings("unused")
public class Json extends HashMap<String, Object> {

    public Json() {
        super();
        this.put("success", false);
        this.put("msg", "");
        this.put("obj", null);
        this.put("errorCode", 0);
    }

    public Json(JsonMessage jsonMessage) {
        super();
        this.put("success", jsonMessage.isSuccess());
        this.put("msg", jsonMessage.getUserMsg());
        this.put("obj", null);
        this.put("errorCode", jsonMessage.getCode());
    }

    public Json(JsonMessage jsonMessage, String msg) {
        super();
        this.put("success", jsonMessage.isSuccess());
        this.put("msg", msg);
        this.put("obj", null);
        this.put("errorCode", jsonMessage.getCode());
    }

    public Json(JsonMessage jsonMessage, Map<String, Object> attributes) {
        super();
        this.put("success", jsonMessage.isSuccess());
        this.put("msg", jsonMessage.getMsg());
        this.putAll(attributes);
        this.put("errorCode", jsonMessage.getCode());
    }

    public Json(JsonMessage jsonMessage, Object obj) {
        super();
        this.put("success", jsonMessage.isSuccess());
        this.put("msg", jsonMessage.getMsg());
        this.put("obj", obj);
        this.put("errorCode", jsonMessage.getCode());
    }

    public Json(JsonMessage jsonMessage, Map<String, Object> attributes, String msg) {
        super();
        this.put("success", jsonMessage.isSuccess());
        this.put("msg", msg);
        this.putAll(attributes);
        this.put("errorCode", jsonMessage.getCode());
    }

    public Json(JsonMessage jsonMessage, Object obj, String msg) {
        super();
        this.put("success", jsonMessage.isSuccess());
        this.put("msg", msg);
        this.put("obj", obj);
        this.put("errorCode", jsonMessage.getCode());
    }

    public boolean isSuccess() {
        return (boolean) this.get("success");
    }

    public void setSuccess(boolean success) {
        this.put("success", success);
    }

    public String getMsg() {
        return (String) this.get("msg");
    }

    public void setMsg(String msg) {
        this.put("msg", msg);
    }

    public Object getObj() {
        return this.get("obj");
    }

    public void setObj(Object obj) {
        this.put("obj", obj);
    }

    public Object getErrorCode() {
        return this.get("errorCode");
    }

    public void setErrorCode(int errorCode) {
        this.put("errorCode", errorCode);
    }

}
