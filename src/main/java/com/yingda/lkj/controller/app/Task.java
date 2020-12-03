package com.yingda.lkj.controller.app;

import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjTask;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * @author hood  2020/2/17
 */
@Data
public class Task {

    private String id;
    private String userId; // 执行人
    private String name; // 任务名
    private List<TaskLkjDataLine> lkjDataLines; // lkj测量批次
    private Timestamp addTime;

    public Task() {
    }

    public Task(LkjTask lkjTask) {
        this.id = lkjTask.getId();
        this.userId = lkjTask.getSubmitUserId();
        this.name = lkjTask.getName();
        this.addTime = lkjTask.getAddTime();
    }
}


@Data
class TaskLkjGroup {
    private List<TaskLkjDataLine> lkjDataLines; // lkj数据
}

@Data
class TaskLkjDataLine {
    public TaskLkjDataLine(String id, TaskDevice leftDevice, TaskDevice rightDevice, String downriver, String retrograde) {
        this.id = id;
        this.deviceList = List.of(leftDevice, rightDevice);
        this.downriver = downriver;
        this.retrograde = retrograde;
        this.distance = 0;
    }

    public TaskLkjDataLine(LkjDataLine lkjDataLine) {
        this.id = lkjDataLine.getId();
        this.downriver = lkjDataLine.getDownriver() + "";
        this.retrograde = lkjDataLine.getRetrograde() + "";
        this.distance = lkjDataLine.getDistance();
//        if (lkjDataLine.getReadonly().equals(LkjDataLine.READ_ONLY))
//            this.readonly = true;
//        if (lkjDataLine.getReadonly().equals(LkjDataLine.EDITABLE))
//            this.readonly = false;
        if (lkjDataLine.getReadonly() != null && lkjDataLine.getReadonly().equals(LkjDataLine.READ_ONLY))
            this.readonly = true;
        else
            this.readonly = false;
        this.lkjDataLine = lkjDataLine;
    }

    private String id;
    private String downriver; // 下行线还是上行线
    private String retrograde; // 正向还是逆向
    private String leftDeviceId; // 起信号机
    private String rightDeviceId; // 止信号机
    private boolean readonly;
    private List<TaskDevice> deviceList;
    private LkjDataLine lkjDataLine;
    private double distance;
}

