package com.yingda.lkj.service.impl.backstage.organization;

import com.yingda.lkj.annotation.CacheMethod;
import com.yingda.lkj.beans.Constant;
import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.backstage.organization.OrganizationService;
import com.yingda.lkj.utils.pojo.PojoUtils;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hood  2019/12/26
 */
@CacheMethod
@Service("organizationService")
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    @Autowired
    private BaseDao<Organization> organizationBaseDao;

    // key:id
    private static final Map<String, Organization> ORGANIZATION_MAP = new HashMap<>();

    @Override
    public void saveOrUpdate(Organization organization) {
        init();
        organizationBaseDao.saveOrUpdate(organization);
        ORGANIZATION_MAP.put(organization.getId(), organization);
    }

    @Override
    public void delete(List<String> ids) {
        init();

        ids.forEach(ORGANIZATION_MAP::remove);
        organizationBaseDao.executeHql(
                "delete from Organization where id in (:ids)",
                Map.of("ids", ids)
        );
    }

    @Override
    public Organization getByName(String name) {
        init();

        return ORGANIZATION_MAP.values().stream().filter(x -> x.getName().equals(name)).reduce(null, (x, y) -> y);
    }

    @Override
    public List<Organization> showDown() {
        init();

        List<Organization> collectFromCache = new ArrayList<>(ORGANIZATION_MAP.values())
                .stream()
                .sorted(Comparator.comparingInt(Organization::getSeq))
                .collect(Collectors.toList());

        try {
            // 直接返回内存，可能会被操作，所以返回一个复制的
            return PojoUtils.copyPojolList(collectFromCache, Organization.class);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("反射异常，不可能出错的，除非改了构造方法", e);
            return null;
        }
    }

    @Override
    public Organization getById(String id) {
        init();
        Organization organizationFromCache = Optional.ofNullable(ORGANIZATION_MAP.get(id)).orElse(new Organization());

        // 直接返回内存，可能会被操作，所以返回一个复制的
        return PojoUtils.copyPojo(organizationFromCache, new Organization());
    }

    @Override
    public List<Organization> getCompleteTree(String sectionId) {
        init();
        List<Organization> returnList = new ArrayList<>();

        Organization section = getById(sectionId);
        returnList.add(section);
        if (section == null)
            return returnList;

        Organization bureau = getById(section.getParentId());
        returnList.add(bureau);

        List<Organization> workshops = showDown().stream().filter(x -> x.getParentId().equals(sectionId)).collect(Collectors.toList());
        returnList.addAll(workshops);
        if (workshops.isEmpty())
            return returnList;

        List<String> workshopIds = workshops.stream().map(Organization::getId).collect(Collectors.toList());

        List<Organization> workAreas = showDown().stream().filter(x -> workshopIds.contains(x.getParentId())).collect(Collectors.toList());
        returnList.addAll(workAreas);

        return jsonified(returnList).stream().sorted(Comparator.comparing(Organization::getSeq)).collect(Collectors.toList());
    }

    @Override
    public List<Organization> jsonified(List<Organization> raw) {
        init();

        // 内存操作，不扣性能，这么写好看
        // 分别是 局，段，车间，工区
        List<Organization> bureaus = raw.stream().filter(x -> x.getLevel() == Organization.BUREAU).collect(Collectors.toList());
        List<Organization> sections = raw.stream().filter(x -> x.getLevel() == Organization.SECTION).collect(Collectors.toList());
        List<Organization> workshops = raw.stream().filter(x -> x.getLevel() == Organization.WORKSHOP).collect(Collectors.toList());
        List<Organization> workAreas = raw.stream().filter(x -> x.getLevel() == Organization.WORK_AREA).collect(Collectors.toList());

        // 如果没有段，返回局
        if (sections.isEmpty())
            return bureaus;

        // 如果有段，把段list按照parentId装到局list内，下面类似
        bureaus.forEach(master -> master.setOrganizationList(
                sections.stream().filter(slave -> slave.getParentId().equals(master.getId())).collect(Collectors.toList())
        ));

        if (workshops.isEmpty())
            return bureaus;

        sections.forEach(master -> master.setOrganizationList(
                workshops.stream().filter(slave -> slave.getParentId().equals(master.getId())).collect(Collectors.toList())
        ));

        if (workAreas.isEmpty())
            return bureaus;

        workshops.forEach(master -> master.setOrganizationList(
                workAreas.stream().filter(slave -> slave.getParentId().equals(master.getId())).collect(Collectors.toList())
        ));

        return bureaus;
    }

    @Override
    public List<Organization> getCompleteTree() {
        init();
        return jsonified(showDown()).stream().sorted(Comparator.comparing(Organization::getSeq)).collect(Collectors.toList());
    }


    private void init() {
        if (!ORGANIZATION_MAP.isEmpty())
            return;

        List<Organization> organizations = organizationBaseDao.find(
                "from Organization"
        );

        ORGANIZATION_MAP.putAll(
                organizations.stream().collect(Collectors.toMap(Organization::getId, x -> x))
        );
    }
}
