package com.yingda.lkj.service.backstage.lkjdataline;

import com.yingda.lkj.beans.entity.backstage.lkj.LkjDataLine;

import java.util.List;

/**
 * lkj分组service
 * uniqueCode相同的lkjDataLine会被分到同一个lkjGroup下
 *
 * @author hood  2020/3/23
 */
public interface LkjGroupService {
    /**
     * 生成lkjGroup
     * @param lkjDataLines 相同uniqueCode的lkjDataLine会被分到同一组中
     */
    void add(List<LkjDataLine> lkjDataLines);
}
