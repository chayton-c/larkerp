package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.line.LineNodePojo;
import com.yingda.lkj.beans.pojo.utils.ExcelSheetInfo;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.comparator.RailwayLineComparator;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.line.FragmentService;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.line.StationRailwayLineService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.RequestUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.excel.ExcelUtil;
import com.yingda.lkj.utils.file.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 线路管理
 *
 * @author hood  2020/1/2
 */
@RequestMapping("/backstage/line")
@Controller
public class LineController extends BaseController {

    @Autowired
    private BaseService<RailwayLine> railwayLineBaseService;
    @Autowired
    private StationRailwayLineService stationRailwayLineService;
    @Autowired
    private BaseService<Fragment> fragmentBaseService;
    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private FragmentService fragmentService;

    private RailwayLine pageRailwayLine;

    @ModelAttribute
    public void setPageRailwayLine(RailwayLine pageRailwayLine) {
        this.pageRailwayLine = pageRailwayLine;
    }

    private Fragment pageFragment;

    @ModelAttribute
    public void setPageFragment(Fragment pageFragment) {
        this.pageFragment = pageFragment;
    }

    /**
     * 线路管理页加载
     */
    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        return null;
    }

    /**
     * 线路详情
     */
    @RequestMapping("/railwayLineInfo")
    public ModelAndView railwayLineInfo() throws Exception {
        User user = RequestUtil.getUser(req);

        List<Organization> bureaus = List.of(organizationClientService.getById(user.getBureauId()));

        String railwayLineId = pageRailwayLine.getId();

        RailwayLine railwayLine = StringUtils.isEmpty(railwayLineId) ?
                new RailwayLine() : // 添加
                railwayLineBaseService.get(RailwayLine.class, railwayLineId); // 修改

        railwayLine.setBureauId(user.getBureauId());

        return new ModelAndView("/backstage/line/railway-line-info", Map.of("railwayLine", railwayLine, "bureaus", bureaus));
    }

    /**
     * 添加/修改线路
     */
    @RequestMapping("/railwayLine/add")
    @ResponseBody
    public Json addRailyway() throws Exception {
        String id = pageRailwayLine.getId();
        if (StringUtils.isEmpty(id)) {
            pageRailwayLine.setId(UUID.randomUUID().toString());
            pageRailwayLine.setAddTime(current());
        }

        try {
            pageRailwayLine.setUpdateTime(current());
            railwayLineBaseService.saveOrUpdate(pageRailwayLine);
        } catch (JpaSystemException e) {
            String errorMsg = e.getCause().getCause().getCause().getMessage();
            return new Json(JsonMessage.PARAM_INVALID, errorMsg);
        }

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 删除线路
     */
    @RequestMapping("/railwayLine/delete")
    @ResponseBody
    public Json deleteRailway(String id) {
        railwayLineService.deleteRailwayLines(Collections.singletonList(id));
        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 区间详情
     */
    @RequestMapping("/fragmentInfo")
    public ModelAndView fragmentInfo(String railwayLineId) throws Exception {
        List<Organization> workAreas = organizationClientService.getWorkAreasBySectionId(getSectionId());

        String fragmentId = pageFragment.getId();
        Fragment fragment = StringUtils.isEmpty(fragmentId) ?
                new Fragment() : // 添加页
                fragmentBaseService.get(Fragment.class, fragmentId); // 修改页

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("fragment", fragment);
        attributes.put("workAreas", workAreas); // 工区下拉选
        attributes.put("railwayLineId", railwayLineId); // 线路id

        return new ModelAndView("/backstage/line/fragment-info", attributes);
    }

    /**
     * 添加/修改区间
     */
    @RequestMapping("/fragment/add")
    @ResponseBody
    public Json addFragment() throws Exception {
        String id = pageFragment.getId();
        if (StringUtils.isEmpty(id)) {
            pageFragment.setId(UUID.randomUUID().toString());
            pageFragment.setAddTime(current());
        } else {
            Fragment fragment = fragmentBaseService.get(Fragment.class, id);
            pageFragment.setName(pageFragment.getName().trim());
            pageFragment.setAddTime(fragment.getAddTime());
            pageFragment.setUpdateTime(current());
        }

        try {
            pageFragment.setUpdateTime(current());
            fragmentBaseService.saveOrUpdate(pageFragment);
        } catch (JpaSystemException e) {
            String errorMsg = e.getCause().getCause().getCause().getMessage();
            return new Json(JsonMessage.PARAM_INVALID, errorMsg);
        }

        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 删除区间
     */
    @RequestMapping("/fragment/delete")
    @ResponseBody
    public Json deleteFragment(String id) throws Exception {
        fragmentBaseService.executeHql("delete from Fragment where id = :id", Map.of("id", id));
        return new Json(JsonMessage.SUCCESS);
    }

    /**
     * 修改区间排序
     */
    @RequestMapping("/updateSeq")
    @ResponseBody
    public Json updateSeq(String id, String seq) throws Exception {
        Fragment fragment = fragmentBaseService.get(Fragment.class, id);
        fragment.setSeq(Integer.parseInt(seq));
        fragment.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        fragmentBaseService.saveOrUpdate(fragment);

        return new Json(JsonMessage.SUCCESS);
    }


    /**
     * 导入线路
     */
    @RequestMapping("/importLines")
    @ResponseBody
    public Json importLines(MultipartFile file) throws Exception {
        List<ExcelSheetInfo> excelSheetInfos = ExcelUtil.readExcelFile(file);
        return railwayLineService.importRailwayLine(excelSheetInfos);
    }


    @RequestMapping("/importOrDownloadTemplate")
    public ModelAndView importOrDownload() {
        return new ModelAndView("/backstage/line/import-and-download");
    }

    @RequestMapping("/lineExcelTemplate")
    @ResponseBody
    public void lineExcelTemplate() throws CustomException, IOException {
        export(FileUtil.readFiles("/static/uploadimg/lineExcelTemplate.xlsx"));
    }

    /**
     * 车站关联线路列表
     */
    @RequestMapping("/stationLineList")
    public ModelAndView stationLineList() throws Exception {
        return new ModelAndView("/backstage/line/station-line-list", attributes);
    }
}
