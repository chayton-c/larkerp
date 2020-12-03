package com.yingda.lkj.controller.backstage;

import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersion;
import com.yingda.lkj.beans.entity.backstage.dataversion.DataVersionUpdateDetail;
import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.line.RailwayLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.entity.system.User;
import com.yingda.lkj.beans.enums.dataversion.ApproveDataType;
import com.yingda.lkj.beans.exception.CustomException;
import com.yingda.lkj.beans.pojo.device.Semaphore;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.controller.BaseController;
import com.yingda.lkj.service.backstage.dataversion.DataVersionService;
import com.yingda.lkj.service.backstage.line.FragmentService;
import com.yingda.lkj.service.backstage.line.RailwayLineService;
import com.yingda.lkj.service.backstage.lkjdataline.LkjDataLineService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StreamUtil;
import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.excel.excelClient.LkjDataExcelParser;
import com.yingda.lkj.utils.hql.HqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/4/16
 */
@RequestMapping("/backstage/lkjVersion")
@Controller
@Component
public class LkjVersionController extends BaseController {

    @Autowired
    OrganizationClientService organizationClientService;
    @Autowired
    private BaseService<LkjDataLine> lkjDataLineBaseService;
    @Autowired
    private BaseService<Fragment> fragmentBaseService;
    @Autowired
    private LkjDataLineService lkjDataLineService;
    @Autowired
    private BaseService<BigInteger> bigIntegerBaseService;
    @Autowired
    private FragmentService fragmentService;
    @Autowired
    private RailwayLineService railwayLineService;
    @Autowired
    private DataVersionService dataVersionService;

    @RequestMapping("")
    public ModelAndView getList() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        String sectionId = getSectionId();

        ApproveDataType.setComponentsAttributes(req, attributes, ApproveDataType.VERSION_PAGE_ROUTING_URL);

        String lkjVersionId = req.getParameter("lkjVersionId");
        String compareLkjVersionId = req.getParameter("compareLkjVersionId"); // 给版本比较用，不影响查询
        String railwayLineId = req.getParameter("railwayLineId");
        String fragmentId = req.getParameter("fragmentId");
        String downriver = req.getParameter("downriver");
        String retrograde = req.getParameter("retrograde");
        String tableTypeStr = req.getParameter("tableTypeStr");

        // 查询参数
        attributes.put("railwayLineId", railwayLineId);
        attributes.put("compareLkjVersionId", compareLkjVersionId);
        attributes.put("fragmentId", fragmentId);
        attributes.put("downriver", downriver);
        attributes.put("retrograde", retrograde);
        attributes.put("tableTypeStr", tableTypeStr);
        attributes.put("page", page);

        List<Fragment> fragments = fragmentService.getFragmentsBySectionIds(sectionId);
        if (StringUtils.isNotEmpty(railwayLineId))
            fragments = fragments.stream().filter(x -> railwayLineId.equals(x.getRailwayLineId())).collect(Collectors.toList());

        // 线路筛选查询
        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(getUser().getBureauId());
        // 区间筛选查询
        fragments.sort(Comparator.comparingInt(Fragment::getSeq));
        // 版本号筛选查询
        List<DataVersion> dataVersions = dataVersionService.getAllVersions(ApproveDataType.LKJ14, sectionId);
        if (dataVersions.isEmpty())
            throw new CustomException(new Json(JsonMessage.DATA_NO_COMPLETE, "尚未生成版本"));
        double dataVersionNumber;
        if (StringUtils.isEmpty(lkjVersionId)) {
            DataVersion dataVersion = dataVersions.get(0);
            lkjVersionId = dataVersion.getId();
            dataVersionNumber = dataVersion.getVersionNumber();
        } else {
            DataVersion dataVersion = dataVersionService.getById(lkjVersionId);
            lkjVersionId = dataVersion.getId();
            dataVersionNumber = dataVersion.getVersionNumber();
        }

        List<String> outdatedDataIds = dataVersionService.getOutdatedDataIds(ApproveDataType.LKJ14, sectionId, dataVersionNumber);

        Map<String, Object> params = new HashMap<>();

        // 先查询unique_code(一组lkj数有相同的先查询unique_code)，再用unique_code换lkj
        params.put("approveStatue", LkjDataLine.APPROVED);
        params.put("fragmentIds", fragments.stream().map(Fragment::getId).collect(Collectors.toList()));
        params.put("dataVersionNumber", dataVersionNumber);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  unique_code as uniqueCode\n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  lkj_data_line \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  approve_status = :approveStatue \n");
        sqlBuilder.append("  AND fragment_id in :fragmentIds \n");
        sqlBuilder.append("  AND data_version_number <= :dataVersionNumber \n");
        if (!outdatedDataIds.isEmpty()) {
            sqlBuilder.append("  AND id not in :outdatedDataIds \n");
            params.put("outdatedDataIds", outdatedDataIds);
        }
        if (StringUtils.isNotEmpty(fragmentId)) {
            sqlBuilder.append("AND fragment_id = :fragmentId \n");
            params.put("fragmentId", fragmentId);
        }
        // 查询线路下的所有区间，再用这些区间查询
        if (StringUtils.isNotEmpty(railwayLineId)) {
            List<Fragment> checkFragments = fragmentBaseService.find("from Fragment where railwayLineId = :railwayLineId",
                    Map.of("railwayLineId", railwayLineId));
            List<String> checkFragmentIds = checkFragments.stream().map(Fragment::getId).collect(Collectors.toList());
            sqlBuilder.append("AND fragment_id in :fragmentIds \n");
            params.put("fragmentIds", checkFragmentIds);
        }
        if (StringUtils.isNotEmpty(downriver)) {
            sqlBuilder.append("AND downriver = :downriver \n");
            params.put("downriver", downriver);
        }
        if (StringUtils.isNotEmpty(retrograde)) {
            sqlBuilder.append("AND retrograde = :retrograde \n");
            params.put("retrograde", retrograde);
        }
        if (StringUtils.isNotEmpty(tableTypeStr)) {
            sqlBuilder.append("AND table_type = :tableType \n");
            params.put("tableType", Byte.valueOf(tableTypeStr));
        }
        sqlBuilder.append(" GROUP BY\n");
        sqlBuilder.append("  unique_code \n");
        sqlBuilder.append(" ORDER BY\n");
        sqlBuilder.append("  seq");
        List<LkjDataLine> uniqueCodeItems = lkjDataLineBaseService.findSQL(sqlBuilder.toString(), params, LkjDataLine.class, page.getCurrentPage(),
                page.getPageSize());
        List<String> uniqueCodes = uniqueCodeItems.stream().map(LkjDataLine::getUniqueCode).collect(Collectors.toList());
        String countSql = HqlUtils.getCountSql(sqlBuilder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal((long) count.size());

        params.clear();
        String hql = "from LkjDataLine where uniqueCode in :uniqueCodes and approveStatus = :approveStatus ";
        if (!outdatedDataIds.isEmpty()) {
            hql += "and id not in :outdatedDataIds";
            params.put("outdatedDataIds", outdatedDataIds);
        }
        params.put("uniqueCodes", uniqueCodes);
        params.put("approveStatus", LkjDataLine.APPROVED);
        List<LkjDataLine> lkjDataLines = lkjDataLineBaseService.find(
                hql + " order by seq", params
        );
        List<Semaphore> semaphores = lkjDataLineService.expandLkjDataLine(lkjDataLines);

        attributes.put("semaphores", semaphores);
        attributes.put("railwayLines", railwayLines);
        attributes.put("fragments", fragments);
        attributes.put("lkjVersions", dataVersions);
        attributes.put("lkjVersionId", lkjVersionId);

        return new ModelAndView("/backstage/lkj-version/lkj-version", attributes);
    }

    @RequestMapping("/dataVersionInfo")
    public ModelAndView dataVersionInfo() {
        Map<String, Object> attributes = new HashMap<>();

        String dataTypeId = req.getParameter("dataTypeId");
        List<DataVersion> allVersions = dataVersionService.getAllVersions(ApproveDataType.getById(dataTypeId), getSectionId());
        List<DataVersion> interimVersions = allVersions.stream() // 所有临时版本
                .filter(x -> DataVersion.INTERIM_VERSION == x.getType()).collect(Collectors.toList());

        attributes.put("interimVersions", interimVersions);

        return new ModelAndView("/backstage/data-version/data-version-info", attributes);
    }

    /**
     * 创建稳定版本
     */
    @RequestMapping("/createStableDataVersion")
    @ResponseBody
    public Json createStableDataVersion() {
        String versionName = req.getParameter("versionName");
        String dataVersionId = req.getParameter("dataVersionId");
        if (StringUtils.isNotEmpty(versionName))
            dataVersionService.createStableDataVersion(dataVersionId, versionName);
        return new Json(JsonMessage.SUCCESS);
    }

    @RequestMapping("/compare")
    public ModelAndView compare() throws Exception {
        String baseVersionId = req.getParameter("baseVersionId");
        String compareLkjVersionId = req.getParameter("compareLkjVersionId");
        String operation = req.getParameter("operation"); //对照版本 检索的 操作字段值

        Map<String, Object> attributes = new HashMap<>();
        List<DataVersionUpdateDetail> updateDetails =
                dataVersionService.compare(ApproveDataType.LKJ14, getSectionId(), baseVersionId, compareLkjVersionId);

        List<String> modifiedLkjDataLineIds = new ArrayList<>();
        modifiedLkjDataLineIds.addAll(StreamUtil.getList(updateDetails, DataVersionUpdateDetail::getPreviousDataId));
        modifiedLkjDataLineIds.addAll(StreamUtil.getList(updateDetails, DataVersionUpdateDetail::getCurrentDataId));
        modifiedLkjDataLineIds = modifiedLkjDataLineIds.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList()); // idList中去掉null

        List<LkjDataLine> lkjDataLines = lkjDataLineBaseService.find(
                "from LkjDataLine where id in :ids",
                Map.of("ids", modifiedLkjDataLineIds)
        );
        Map<String, LkjDataLine> lkjDataLineMap = StreamUtil.getMap(lkjDataLines, LkjDataLine::getId, x -> x);

        List<LkjDataLine> modifiedLkjDataLines = new ArrayList<>();
        for (DataVersionUpdateDetail updateDetail : updateDetails) {
            String previousDataId = updateDetail.getPreviousDataId();
            String currentDataId = updateDetail.getCurrentDataId();

            LkjDataLine previousData = previousDataId == null ? null : lkjDataLineMap.get(previousDataId);
            LkjDataLine currentData = currentDataId == null ? null : lkjDataLineMap.get(currentDataId);

            if (DataVersionUpdateDetail.UPDATE == updateDetail.getOperationType()) {
                double previousDistance = previousData.getDistance();
                currentData.setOperation("重新测量");
                currentData.setPreviousDistance(previousDistance);
                modifiedLkjDataLines.add(currentData);
            }

            if (DataVersionUpdateDetail.ADD == updateDetail.getOperationType()) {
                currentData.setOperation("新增");
                currentData.setPreviousDistance(0.0);
                modifiedLkjDataLines.add(currentData);
            }

            if (DataVersionUpdateDetail.DELETE == updateDetail.getOperationType()) {
                previousData.setOperation("删除");
                previousData.setPreviousDistance(0.0);
                modifiedLkjDataLines.add(previousData);
            }
        }

        modifiedLkjDataLines = lkjDataLineService.fillLkjDataLineDevice(modifiedLkjDataLines);
        modifiedLkjDataLines = linkage(modifiedLkjDataLines);

//        if (StringUtils.isNotEmpty(operation))
//            lkjDataLines = lkjDataLines.stream().filter(x -> operation.equals(x.getOperation())).collect(Collectors.toList());

        attributes.put("lkjDataLines", modifiedLkjDataLines);
        attributes.put("baseVersionId", baseVersionId);
        attributes.put("compareLkjVersionId", compareLkjVersionId);
        attributes.put("operation", operation);

        return new ModelAndView("/backstage/lkj-version/lkj-compare", attributes);
    }


    @RequestMapping("/export")
    @ResponseBody
    public void export() throws Exception {
        User user = getUser();

        Map<String, Object> attributes = new HashMap<>();
        String sectionId = getSectionId();

        ApproveDataType.setComponentsAttributes(req, attributes, ApproveDataType.VERSION_PAGE_ROUTING_URL);

        String lkjVersionId = req.getParameter("lkjVersionId");
        String compareLkjVersionId = req.getParameter("compareLkjVersionId"); // 给版本比较用，不影响查询
        String railwayLineId = req.getParameter("railwayLineId");
        String fragmentId = req.getParameter("fragmentId");
        String downriver = req.getParameter("downriver");
        String retrograde = req.getParameter("retrograde");

        // 查询参数
        attributes.put("railwayLineId", railwayLineId);
        attributes.put("compareLkjVersionId", compareLkjVersionId);
        attributes.put("fragmentId", fragmentId);
        attributes.put("downriver", downriver);
        attributes.put("retrograde", retrograde);
        attributes.put("page", page);

        List<Fragment> fragments = fragmentService.getFragmentsBySectionIds(sectionId);
        if (StringUtils.isNotEmpty(railwayLineId))
            fragments = fragments.stream().filter(x -> railwayLineId.equals(x.getRailwayLineId())).collect(Collectors.toList());

        // 线路筛选查询
        List<RailwayLine> railwayLines = railwayLineService.getRailwayLinesByBureauId(getUser().getBureauId());
        // 区间筛选查询
        fragments.sort(Comparator.comparingInt(Fragment::getSeq));
        // 版本号筛选查询
        List<DataVersion> dataVersions = dataVersionService.getAllVersions(ApproveDataType.LKJ14, sectionId);
        if (dataVersions.isEmpty())
            throw new CustomException(new Json(JsonMessage.DATA_NO_COMPLETE, "尚未生成版本"));
        double dataVersionNumber;
        if (StringUtils.isEmpty(lkjVersionId)) {
            DataVersion dataVersion = dataVersions.get(0);
            lkjVersionId = dataVersion.getId();
            dataVersionNumber = dataVersion.getVersionNumber();
        } else {
            DataVersion dataVersion = dataVersionService.getById(lkjVersionId);
            lkjVersionId = dataVersion.getId();
            dataVersionNumber = dataVersion.getVersionNumber();
        }

        List<String> outdatedDataIds = dataVersionService.getOutdatedDataIds(ApproveDataType.LKJ14, sectionId, dataVersionNumber);

        Map<String, Object> params = new HashMap<>();

        // 先查询unique_code(一组lkj数有相同的先查询unique_code)，再用unique_code换lkj
        params.put("approveStatue", LkjDataLine.APPROVED);
        params.put("fragmentIds", fragments.stream().map(Fragment::getId).collect(Collectors.toList()));
        params.put("dataVersionNumber", dataVersionNumber);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT\n");
        sqlBuilder.append("  unique_code as uniqueCode\n");
        sqlBuilder.append("FROM\n");
        sqlBuilder.append("  lkj_data_line \n");
        sqlBuilder.append("WHERE\n");
        sqlBuilder.append("  approve_status = :approveStatue \n");
        sqlBuilder.append("  AND fragment_id in :fragmentIds \n");
        sqlBuilder.append("  AND data_version_number <= :dataVersionNumber \n");
        if (!outdatedDataIds.isEmpty()) {
            sqlBuilder.append("  AND id not in :outdatedDataIds \n");
            params.put("outdatedDataIds", outdatedDataIds);
        }
        if (StringUtils.isNotEmpty(fragmentId)) {
            sqlBuilder.append("AND fragment_id = :fragmentId ");
            params.put("fragmentId", fragmentId);
        }
        // 查询线路下的所有区间，再用这些区间查询
        if (StringUtils.isNotEmpty(railwayLineId)) {
            List<Fragment> checkFragments = fragmentBaseService.find("from Fragment where railwayLineId = :railwayLineId",
                    Map.of("railwayLineId", railwayLineId));
            List<String> checkFragmentIds = checkFragments.stream().map(Fragment::getId).collect(Collectors.toList());
            sqlBuilder.append("AND fragment_id in :fragmentIds");
            params.put("fragmentIds", checkFragmentIds);
        }
        if (StringUtils.isNotEmpty(downriver)) {
            sqlBuilder.append("AND downriver = :downriver ");
            params.put("downriver", downriver);
        }
        if (StringUtils.isNotEmpty(retrograde)) {
            sqlBuilder.append("AND retrograde = :retrograde ");
            params.put("retrograde", retrograde);
        }
        sqlBuilder.append(" GROUP BY\n");
        sqlBuilder.append("  unique_code \n");
        sqlBuilder.append(" ORDER BY\n");
        sqlBuilder.append("  seq");
        List<LkjDataLine> uniqueCodeItems = lkjDataLineBaseService.findSQL(
                sqlBuilder.toString(), params, LkjDataLine.class, 1, 999999);
        List<String> uniqueCodes = uniqueCodeItems.stream().map(LkjDataLine::getUniqueCode).collect(Collectors.toList());
        String countSql = HqlUtils.getCountSql(sqlBuilder.toString());
        List<BigInteger> count = bigIntegerBaseService.findSQL(countSql, params);
        page.setDataTotal((long) count.size());

        List<LkjDataLine> lkjDataLines = lkjDataLineBaseService.find(
                "from LkjDataLine where uniqueCode in :uniqueCodes and approveStatus = :approveStatus order by seq",
                Map.of("uniqueCodes", uniqueCodes, "approveStatus", LkjDataLine.APPROVED)
        );
        List<Semaphore> semaphores = lkjDataLineService.expandLkjDataLine(lkjDataLines);

        String bureauId = user.getBureauId();
        Organization bureau = organizationClientService.getById(bureauId);
        MultipartFile workbookFile = new LkjDataExcelParser(bureau.getCode()).createWorkbook(lkjDataLines, semaphores);
        export(workbookFile);
    }

    /**
     * <p>遍历在同一组(lkjGroupId相同的lkjDataLine)的lkj，</p>
     * <p>按顺序(seq)填写一组lkj中的前一个lkjId和后一个lkjId</p>
     * <p>用于生成任务</p>
     */
    private List<LkjDataLine> linkage(List<LkjDataLine> lkjDataLines) {
        Map<String, List<LkjDataLine>> lkjDataLineMap = lkjDataLines.stream()
                .sorted(Comparator.comparingInt(LkjDataLine::getSeq))
                .collect(Collectors.groupingBy(LkjDataLine::getLkjGroupId));

        List<LkjDataLine> returnList = new ArrayList<>();
        for (List<LkjDataLine> group : lkjDataLineMap.values()) {
            for (int i = 0; i < group.size(); i++) {
                LkjDataLine current = group.get(i);

                if (i != 0)
                    current.setPreviousLkjDataLineId(group.get(i - 1).getId());

                if (i != (group.size() - 1))
                    current.setNextLkjDataLineId(group.get(i + 1).getId());

                returnList.add(current);
            }
        }
        return returnList;
    }

}
