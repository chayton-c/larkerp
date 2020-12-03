package com.yingda.lkj.controller.app;

import lombok.Data;

import java.util.UUID;

/**
 * @author hood  2020/4/10
 */
@Data
public class TaskDevice {
    public TaskDevice() {
    }

    private String id;
    private String bureauName; // 局名
    private String bureauCode; // 局编号
    private String lineName; // 线名
    private String lineCode; // 线编号
    private String downriver; // 下行信号机还是上行信号机
    private String stationId; // 车站id
    private String stationName; // 车站名
    private String code; // 信号机code
    private String name; // 信号机name
}
