package com.yingda.lkj.controller.orgTree.util;

import java.util.HashMap;
import java.util.List;

public class LayuiUtil extends HashMap<String, Object> {
    public static LayuiUtil data(Integer count, List<?> data) {

        LayuiUtil layuiUtil = new LayuiUtil();
        layuiUtil.put("code", 0);
        layuiUtil.put("msg", "");
        layuiUtil.put("count", count);
        layuiUtil.put("data", data);

        return layuiUtil;
    }


    public static LayuiUtil data(Object obj) {
        Object [] data = {obj};
        LayuiUtil layuiUtil = new LayuiUtil();
        layuiUtil.put("code", 0);
        layuiUtil.put("msg", "");
        layuiUtil.put("count", 1);
        layuiUtil.put("data", data);

        return layuiUtil;
    }

}
