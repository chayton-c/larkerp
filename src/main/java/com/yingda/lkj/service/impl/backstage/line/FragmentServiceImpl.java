package com.yingda.lkj.service.impl.backstage.line;

import com.yingda.lkj.beans.entity.backstage.line.Fragment;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.line.FragmentService;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hood  2020/2/21
 */
@Service("fragmentService")
public class FragmentServiceImpl implements FragmentService {

    @Autowired
    private OrganizationClientService organizationClientService;
    @Autowired
    private BaseDao<Fragment> fragmentBaseDao;

    @Override
    public List<Fragment> getFragmentsBySectionIds(String sectionId) {
        List<Organization> workAreas = organizationClientService.getWorkAreasBySectionId(sectionId);
        if (workAreas.isEmpty())
            return new ArrayList<>();


        List<String> workAreaIds = workAreas.stream().map(Organization::getId).collect(Collectors.toList());
        List<Fragment> fragments = fragmentBaseDao.find("from Fragment where workAreaId in :workAreaIds", Map.of("workAreaIds", workAreaIds));
        return fragments;
    }

    @Override
    public Fragment getByCode(String code) {
        return fragmentBaseDao.get("from Fragment where code = :code", Map.of("code", code));
    }

    @Override
    public Fragment getFramentsByName(String name) {
        return fragmentBaseDao.get("from Fragment where name = :name", Map.of("name", name));
    }

    @Override
    public Fragment getById(String id) {
        return fragmentBaseDao.get(Fragment.class, id);
    }
}
