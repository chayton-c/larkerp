package com.yingda.lkj.controller.app;

import com.yingda.lkj.beans.pojo.app.AppLkjDataLineReceive;
import com.yingda.lkj.beans.pojo.app.AppLkjFreeTaskReceive;
import com.yingda.lkj.beans.pojo.app.AppLkjTaskReceive;
import com.yingda.lkj.beans.pojo.app.AppLocationReceive;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.app.lkj.AppLkjDataLineReceiveService;
import com.yingda.lkj.utils.JsonUtils;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author hood  2020/4/9
 */
@Controller
@RequestMapping("/app/lkj")
public class AppLkjController extends BaseController {

    @Autowired
    private AppLkjDataLineReceiveService appLkjDataLineReceiveService;

    @RequestMapping("/receiveLkjMissions")
    @ResponseBody
    public Json receiveLkjMissions() throws IOException {
        System.out.println("receiveLkjMissions");
        String akagi = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println(akagi);
        List<AppLkjTaskReceive> lkjTaskReceives = JsonUtils.parseList(akagi, AppLkjTaskReceive.class);

        List<AppLkjDataLineReceive> lkjDataLineReceives = new ArrayList<>();
        for (AppLkjTaskReceive lkjTaskReceive : lkjTaskReceives)
            lkjDataLineReceives.addAll(lkjTaskReceive.getLkjDataLines());
        appLkjDataLineReceiveService.saveLkjDataLines(lkjDataLineReceives);
        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/receiveFreeLkjs")
    @ResponseBody
    public Json receiveFreeLkjs() throws IOException {
        System.out.println("receiveFreeLkjs");
        String akagi = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println(akagi);
        AppLkjFreeTaskReceive parse = JsonUtils.parse(akagi, AppLkjFreeTaskReceive.class);
        appLkjDataLineReceiveService.saveFreeLkjDataLines(parse.getLkjDataLines(), parse.getUserId());
        return new Json(JsonMessage.SUCCESS);
    }

    private String mission = "[\n" +
            "  {\n" +
            "    \"addTime\": \"2020-04-15\",\n" +
            "    \"id\": \"015685db-28b9-4acf-ab41-5e1bbe425155\",\n" +
            "    \"isOver\": true,\n" +
            "    \"lkjDataLines\": [\n" +
            "      {\n" +
            "        \"complete_time\": \"1279608898408\",\n" +
            "        \"deviceList\": [\n" +
            "          {\n" +
            "            \"bureauCode\": \"01\",\n" +
            "            \"bureauName\": \"哈尔滨\",\n" +
            "            \"code\": \"X\",\n" +
            "            \"downriver\": \"上行信号机\",\n" +
            "            \"id\": \"64a3a4fa-9bbc-4aab-8d46-2ddf6f59b898\",\n" +
            "            \"lineCode\": \"0102\",\n" +
            "            \"lineName\": \"滨洲\",\n" +
            "            \"locationEnd\": {\n" +
            "              \"altitude\": 0.0,\n" +
            "              \"horizontal\": 0.0,\n" +
            "              \"lat\": 45.7390718161,\n" +
            "              \"latOffset\": 0.0,\n" +
            "              \"lon\": 126.68289485849999\n" +
            "            },\n" +
            "            \"measure_time\": \"1279608890998\",\n" +
            "            \"stationId\": \"098a757a-9aa1-4358-82e9-7e6216b6ead5\",\n" +
            "            \"stationName\": \"庙台子\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"lineName\": \"P1279608892\",\n" +
            "            \"locationEnd\": {\n" +
            "              \"altitude\": 0.0,\n" +
            "              \"horizontal\": 0.0,\n" +
            "              \"lat\": 45.7390685027,\n" +
            "              \"latOffset\": 0.0,\n" +
            "              \"lon\": 126.68287949949999\n" +
            "            },\n" +
            "            \"measure_time\": \"1279608894601\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"bureauCode\": \"01\",\n" +
            "            \"bureauName\": \"哈尔滨\",\n" +
            "            \"code\": \"0107\",\n" +
            "            \"downriver\": \"上行信号机\",\n" +
            "            \"id\": \"815f9a0d-17f7-4804-9fe9-38f3496f4a3e\",\n" +
            "            \"lineCode\": \"0102\",\n" +
            "            \"lineName\": \"滨洲\",\n" +
            "            \"locationEnd\": {\n" +
            "              \"altitude\": 0.0,\n" +
            "              \"horizontal\": 0.0,\n" +
            "              \"lat\": 45.7390740129,\n" +
            "              \"latOffset\": 0.0,\n" +
            "              \"lon\": 126.68284660800002\n" +
            "            },\n" +
            "            \"measure_time\": \"1279608898406\",\n" +
            "            \"stationId\": \"73f05454-7c04-4692-882a-00667857834b\",\n" +
            "            \"stationName\": \"哈尔滨北综合场\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"distance\": \"3.88\",\n" +
            "        \"downriver\": \"1\",\n" +
            "        \"id\": \"0ef72711-154e-479d-87ca-292250edd3e2\",\n" +
            "        \"retrograde\": \"0\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"complete_time\": \"1279608908316\",\n" +
            "        \"deviceList\": [\n" +
            "          {\n" +
            "            \"bureauCode\": \"01\",\n" +
            "            \"bureauName\": \"哈尔滨\",\n" +
            "            \"code\": \"X4\",\n" +
            "            \"downriver\": \"上行信号机\",\n" +
            "            \"id\": \"ec727bc8-9cb8-462f-ad31-a633968a28e9\",\n" +
            "            \"lineCode\": \"0151\",\n" +
            "            \"lineName\": \"滨绥\",\n" +
            "            \"locationEnd\": {\n" +
            "              \"altitude\": 0.0,\n" +
            "              \"horizontal\": 0.0,\n" +
            "              \"lat\": 45.7390648357,\n" +
            "              \"latOffset\": 0.0,\n" +
            "              \"lon\": 126.68283217800001\n" +
            "            },\n" +
            "            \"measure_time\": \"1279608904612\",\n" +
            "            \"stationId\": \"47fd4413-e2a8-411a-83f0-65d749e73af5\",\n" +
            "            \"stationName\": \"爱河\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"bureauCode\": \"01\",\n" +
            "            \"bureauName\": \"哈尔滨\",\n" +
            "            \"code\": \"YX\",\n" +
            "            \"downriver\": \"上行信号机\",\n" +
            "            \"id\": \"fc3ce904-4919-48a1-83a6-d4e7bcde88ff\",\n" +
            "            \"lineCode\": \"30016\",\n" +
            "            \"lineName\": \"爱伊线\",\n" +
            "            \"locationEnd\": {\n" +
            "              \"altitude\": 0.0,\n" +
            "              \"horizontal\": 0.0,\n" +
            "              \"lat\": 45.739053499,\n" +
            "              \"latOffset\": 0.0,\n" +
            "              \"lon\": 126.68283904040001\n" +
            "            },\n" +
            "            \"measure_time\": \"1279608908315\",\n" +
            "            \"stationId\": \"cfa635e1-7a4a-48e0-8a63-380f33684440\",\n" +
            "            \"stationName\": \"老磨刀石\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"distance\": \"1.37\",\n" +
            "        \"downriver\": \"2\",\n" +
            "        \"id\": \"6ad7b4a1-1f4d-42f1-9936-488bf120ef53\",\n" +
            "        \"retrograde\": \"0\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"name\": \"刘迪 lkj测量任务 2020-04-15\",\n" +
            "    \"userId\": \"56ca37f2-7396-4ef2-9795-cea982174408\"\n" +
            "  }\n" +
            "]\n";

    String free = "{\n" +
            "  \"isOver\": false,\n" +
            "  \"lkjDataLines\": [\n" +
            "    {\n" +
            "      \"complete_time\": \"1279609288492\",\n" +
            "      \"deviceList\": [\n" +
            "        {\n" +
            "          \"lineName\": \"2\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"lineName\": \"8\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"distance\": \"12\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"complete_time\": \"1279609301413\",\n" +
            "      \"deviceList\": [\n" +
            "        {\n" +
            "          \"lineName\": \"d\",\n" +
            "          \"locationEnd\": {\n" +
            "            \"altitude\": 0.0,\n" +
            "            \"horizontal\": 0.0,\n" +
            "            \"lat\": 45.7390334762,\n" +
            "            \"latOffset\": 0.0,\n" +
            "            \"lon\": 126.68285629500002\n" +
            "          },\n" +
            "          \"measure_time\": \"1279609293403\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"lineName\": \"P1279609294\",\n" +
            "          \"locationEnd\": {\n" +
            "            \"altitude\": 0.0,\n" +
            "            \"horizontal\": 0.0,\n" +
            "            \"lat\": 45.739027409399995,\n" +
            "            \"latOffset\": 0.0,\n" +
            "            \"lon\": 126.68280437155002\n" +
            "          },\n" +
            "          \"measure_time\": \"1279609297306\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"lineName\": \"8\",\n" +
            "          \"locationEnd\": {\n" +
            "            \"altitude\": 0.0,\n" +
            "            \"horizontal\": 0.0,\n" +
            "            \"lat\": 45.73901717995,\n" +
            "            \"latOffset\": 0.0,\n" +
            "            \"lon\": 126.68269405804998\n" +
            "          },\n" +
            "          \"measure_time\": \"1279609301410\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"distance\": \"12.74\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"userId\": \"ca669ab1-9304-4f49-9a54-3117dc5f1274\"\n" +
            "}";
}
