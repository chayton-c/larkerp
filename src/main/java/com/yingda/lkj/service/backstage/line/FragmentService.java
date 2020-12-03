package com.yingda.lkj.service.backstage.line;

import com.yingda.lkj.beans.entity.backstage.line.Fragment;

import java.util.List;

/**
 * @author hood  2020/2/21
 */
public interface FragmentService {
    /**
     * 获取站段(Organization level = Organization.SECTION)下所有的区间
     */
    List<Fragment> getFragmentsBySectionIds(String sectionId);

    Fragment getByCode(String code);

    Fragment getFramentsByName(String name);

    Fragment getById(String id);
}
