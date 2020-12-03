package com.yingda.lkj.controller.app.task;

import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author hood  2020/7/9
 */
@RequestMapping("/app/measurementTask")
@Controller
public class AppMeasurementTaskController extends BaseController {

    @RequestMapping("")
    @ResponseBody
    public Json getMeasurementTasks() throws CustomException {
        checkParameters("finishedStatus", "pageSize", "currentPage", "userId");

        String finishedStatusStr = req.getParameter("finishedStatus");
        String currentPageStr = req.getParameter("currentPage");
        String pageSizeStr = req.getParameter("pageSize");
        String userId = req.getParameter("userId");

        return null;
    }


}
