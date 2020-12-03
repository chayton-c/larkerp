package com.yingda.lkj.service.impl.backstage.organization;

import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.service.backstage.organization.OrganizationClientService;
import com.yingda.lkj.service.backstage.organization.OrganizationService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hood  2019/12/27
 */
@Service("organizationClientService")
public class OrganizationClientServiceImpl implements OrganizationClientService {

    @Autowired
    private OrganizationService organizationService;

    @Override
    public List<Organization> getSlave(String masterId) {
        List<Organization> expand = organizationService.expand(organizationService.getCompleteTree(), new ArrayList<>());
        Organization masterNode = expand.stream().filter(x -> x.getId().equals(masterId)).reduce(null, (x, y) -> y);

        if (masterNode == null)
            return new ArrayList<>();

        return masterNode.getOrganizationList();
    }

    @Override
    public List<Organization> getWorkAreas(List<String> workshopId) {
        return getWorkAreas().stream().filter(x -> workshopId.contains(x.getParentId())).collect(Collectors.toList());
    }

    @Override
    public Organization getParent(String organizationId) {
        Organization organization = organizationService.getById(organizationId);
        if (organization == null)
            return null;

        String parentId = organization.getParentId();
        if (StringUtils.isEmpty(parentId))
            return null;

        return organizationService.getById(parentId);
    }

    @Override
    public List<Organization> getBureaus() {
        return organizationService.getCompleteTree();
    }

    @Override
    public List<Organization> getSections() {
        return organizationService.showDown().stream().filter(x -> x.getLevel() == Organization.SECTION).collect(Collectors.toList());
    }

    @Override
    public List<Organization> getWorkshops() {
        return organizationService.showDown().stream().filter(x -> x.getLevel() == Organization.WORKSHOP).collect(Collectors.toList());
    }

    @Override
    public List<Organization> getWorkAreas() {
        return organizationService.showDown().stream().filter(x -> x.getLevel() == Organization.WORK_AREA).collect(Collectors.toList());
    }

    @Override
    public List<Organization> getWorkAreasBySectionId(String sectionId) {
        List<Organization> workshops = getSlave(sectionId);
        if (workshops.isEmpty())
            return new ArrayList<>();

        List<String> workshopIds = workshops.stream().map(Organization::getId).collect(Collectors.toList());
        List<Organization> workAreas = getWorkAreas().stream().filter(x -> workshopIds.contains(x.getParentId())).collect(Collectors.toList());
        return workAreas;
    }

    @Override
    public List<Organization> getAllWorkAreas() {
        return organizationService.showDown().stream().filter(x -> x.getLevel() == Organization.WORK_AREA).collect(Collectors.toList());
    }

    @Override
    public Organization getById(String id) {
        return organizationService.getById(id);
    }

    @Override
    public List<Organization> getByIds(List<String> ids) {
        return organizationService.showDown().stream().filter(x -> ids.contains(x.getId())).collect(Collectors.toList());
    }

    @Override
    public List<Organization> getByLevel(byte level) {
        return organizationService.showDown().stream().filter(x -> x.getLevel() == level).collect(Collectors.toList());
    }


    @Override
    public Organization getBureauByWorkareaId(String workAreaId) {
        // 内存方法为所欲为，除非内存改用redis
        Organization workArea = getById(workAreaId);
        Organization workshop = getById(workArea.getParentId());
        Organization section = getById(workshop.getParentId());
        return getById(section.getParentId());
    }

    @Override
    public Organization getBureauByCode(String code) {
        return organizationService.showDown().stream().filter(x -> x.getCode().equals(code)).reduce((x, y) -> x).orElse(null);
    }


}
