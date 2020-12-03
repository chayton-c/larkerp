package com.yingda.lkj.controller.client;

import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.FragmentService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/2/5
 */
@Controller
@RequestMapping("/client/railwayLine")
public class RailwayLineClientController extends BaseController {

    @Autowired
    private BaseService<Fragment> fragmentBaseService;
    @Autowired
    private FragmentService fragmentService;

    /**
     * 通过线路id获取区间list
     */
    @RequestMapping("/getFragments")
    @ResponseBody
    public Json getSlave(String railwayLineId) throws Exception {
        User user = RequestUtil.getUser(req);


        if (user != null) {
            String sectionId = user.getSectionId();
            List<Fragment> fragments = fragmentService.getFragmentsBySectionIds(sectionId);
            fragments = fragments.stream()
                    .filter(fragment -> railwayLineId.equals(fragment.getRailwayLineId()))
                    .sorted(Comparator.comparingInt(Fragment::getSeq))
                    .collect(Collectors.toList());
            return new Json(JsonMessage.SUCCESS, fragments);
        }

        List<Fragment> fragments = fragmentBaseService.find("from Fragment where railwayLineId = :railwayLineId order by seq", Map.of("railwayLineId",
                railwayLineId));

        return new Json(JsonMessage.SUCCESS, fragments);
    }


}
