package com.yingda.lkj.service.backstage.organization;

import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import org.aspectj.weaver.ast.Or;

import java.util.List;

/**
 * <p>organization的客户端方法</p>
 *
 * <p>与organizationService的区别在于</p>
 * <p>organizationService负责Organization的维护(主要操作内存和组织管理页面)</p>
 * <p>而这个service负责实际业务逻辑，没有内存操作</p>
 * <p>实现类中只会调用organizationService，不与数据库交互(不调用dao)</p>
 * <p>总之除了组织管理页下面的方法调用organizationService以外，其他的页面都调这个</p>
 *
 * @author hood  2019/12/27
 */
public interface OrganizationClientService {

    /**
     * 获取下级(局找段，段找车间。。。)
     */
    List<Organization> getSlave(String masterId);

    /**
     * 获取下级工区
     */
    List<Organization> getWorkAreas(List<String> workshopId);

    /**
     * 获取上级
     */
    Organization getParent(String organizationId);

    /**
     * 找所有的局
     */
    List<Organization> getBureaus();

    /**
     * 所有的站段
     */
    List<Organization> getSections();

    /**
     * 所有的车间
     */
    List<Organization> getWorkshops();

    /**
     * 所有的工区
     */
    List<Organization> getWorkAreas();

    /**
     * 站段下所有的工区
     */
    List<Organization> getWorkAreasBySectionId(String sectionId);

    /**
     * 所有的工区
     */
    List<Organization> getAllWorkAreas();

    Organization getById(String id);

    List<Organization> getByIds(List<String> ids);

    /**
     * 获取对应等级的组织
     */
    List<Organization> getByLevel(byte level);

    Organization getBureauByWorkareaId(String workAreaId);

    Organization getBureauByCode(String code);

}
