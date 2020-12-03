package com.yingda.lkj.service.backstage.organization;

import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import org.aspectj.weaver.ast.Or;

import java.util.*;

/**
 * <p>除了组织管理页下面的方法，其他页面不要调用</p>
 * <p>去调OrganizationClientService，先看注释</p>
 * <p>肯定有人直接调这个的</p>
 *
 * @author hood  2019/12/26
 */
public interface OrganizationService {
    void saveOrUpdate(Organization organization);

    void delete(List<String> ids);

    Organization getByName(String name);

    List<Organization> showDown();

    Organization getById(String id);

    List<Organization> getCompleteTree(String sectionId);

    /**
     * <p>把raw中的数据按照parentId折叠到一起(下级放到上级的organizationList中)</p>
     * <p>只查了局，段，车间，工区四级，不过现在只有四级，如果之后要扩展，注意这里的坑(算坑吗)</p>
     */
    List<Organization> jsonified(List<Organization> raw);

    /**
     * 获取完整的组织结构(从局开始到车间，没有根节点)
     */
    List<Organization> getCompleteTree();

    /**
     * <p>如果List<Organization> raw.organizationList中有数据</p>
     * <p>按照展开顺序生成list后返回</p>
     */
    default List<Organization> expand(List<Organization> raw, List<Organization> resultList) {
        for (Organization organization : raw) {
            resultList.add(organization);
            if (organization.getOrganizationList() != null)
                expand(organization.getOrganizationList(), resultList);
        }

        return resultList;
    }

}
