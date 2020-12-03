package com.yingda.lkj.service.impl.backstage.lkjdataline;

import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;
import com.yingda.lkj.beans.entity.backstage.lkj.LkjGroup;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.lkjdataline.LkjGroupService;
import com.yingda.lkj.utils.StreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2020/3/23
 */
@Service("lkjGroupService")
public class LkjGroupServiceImpl implements LkjGroupService {

    @Autowired
    private BaseDao<LkjGroup> lkjGroupBaseDao;
    @Autowired
    private BaseDao<Fragment> fragmentBaseDao;
    @Autowired
    private BaseDao<LkjDataLine> lkjDataLineBaseDao;


    @Override
    public synchronized void add(List<LkjDataLine> lkjDataLines) {
        Map<String, List<LkjDataLine>> lkjGroupMap = lkjDataLines.stream().
                collect(Collectors.groupingBy(LkjDataLine::getUniqueCode));

        // 查询已经存在的group
        Set<String> uniqueCodes = lkjGroupMap.keySet();
        List<LkjGroup> originalGroups = lkjGroupBaseDao.find(
                "from LkjGroup where uniqueCode in :uniqueCodes",
                Map.of("uniqueCodes", uniqueCodes)
        );
        Map<String, LkjGroup> originalGroupMap = originalGroups.stream().collect(Collectors.toMap(LkjGroup::getUniqueCode, x -> x));

        List<LkjGroup> groups = new ArrayList<>();
        List<LkjDataLine> pendingUpdateLkjDataLines = new ArrayList<>();
        int code = getCode(); // 查询自增code
        for (String key : uniqueCodes) {
            List<LkjDataLine> lkjDataLineGroup = lkjGroupMap.get(key);

            byte downriver = lkjDataLineGroup.get(0).getDownriver(); // 传空的报错
            byte retrograde = lkjDataLineGroup.get(0).getRetrograde();
            String fragmentId = lkjDataLineGroup.get(0).getFragmentId();
            String uniqueCode = lkjDataLineGroup.get(0).getUniqueCode();
            code++;

            Fragment fragment = fragmentBaseDao.get(Fragment.class, fragmentId);

            // 是否已经存在uniqueCode相同的lkjGroup,如果有,不添加新的lkjGroup
            LkjGroup lkjGroup = originalGroupMap.get(uniqueCode);
            if (lkjGroup == null) {
                lkjGroup = new LkjGroup(fragment, code, downriver, retrograde, uniqueCode);
                groups.add(lkjGroup);
            }
            String lkjGroupId = lkjGroup.getId();

            lkjDataLineGroup.forEach(x -> x.setLkjGroupId(lkjGroupId));
            pendingUpdateLkjDataLines.addAll(lkjDataLineGroup);
        }

        groups = groups.stream().filter(StreamUtil.distinct(LkjGroup::getUniqueCode)).collect(Collectors.toList());

        lkjGroupBaseDao.bulkInsert(groups);
        lkjDataLineBaseDao.bulkInsert(pendingUpdateLkjDataLines);
    }

    private int getCode() {
        List<LkjGroup> groups = lkjGroupBaseDao.find("from LkjGroup order by code desc", null, 1, 1);
        if (groups.size() == 0)
            return 0;

        return groups.get(0).getCode();
    }
}
