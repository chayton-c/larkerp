package com.yingda.lkj.beans.exception;

import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;

/**
 * @author hood  2019/12/18
 */
public class CustomException extends Exception {
    private Json json;
    private boolean needToLog;

    public CustomException(JsonMessage jsonMessage, String msg) {
        super(msg);
        this.json = new Json(jsonMessage, msg);
        this.needToLog = false;
    }

    public CustomException(Json json) {
        super(json.getMsg());
        this.json = json;
        this.needToLog = false;
    }

    public CustomException(Json json, boolean needToLog) {
        super(json.getMsg());
        this.json = json;
        this.needToLog = needToLog;
    }

    public Json getJson() {
        return json;
    }

    public void setJson(Json json) {
        this.json = json;
    }

    public boolean isNeedToLog() {
        return needToLog;
    }

    public void setNeedToLog(boolean needToLog) {
        this.needToLog = needToLog;
    }

    @Override
    public String toString() {
        return "CustomException{" +
                "json=" + json +
                '}';
    }
}
