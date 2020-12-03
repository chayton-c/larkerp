package com.yingda.lkj.service.base;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author hood  2019/12/13
 */
@SuppressWarnings("unused")
public interface BaseService<T> {

    T get(Class<T> clazz, Serializable id) throws Exception;

    T get(String hql) throws Exception;

    T get(String hql, Map<String, Object> params) throws Exception;

    List<T> find(String hql) throws Exception;

    List<T> find(String hql, int currentPage, int pageSize) throws Exception;

    List<T> find(String hql, Map<String, Object> params) throws Exception;

    List<T> findSQL(String sql, Map<String, Object> params, Class<T> clazz) throws Exception;

    List<T> findSQL(String sql, Map<String, Object> params, Class<T> clazz, int currentPage, int pageSize) throws Exception;

    List<T> find(String hql, Map<String, Object> params, int currentPage, int pageSize) throws Exception;

    /**
     * 批量插入
     */
    void bulkInsert(List<T> list);


    /**
     * 批量插入, 手动设置缓冲区的插入数量，即每buffer条数据提交一次
     */
    void bulkInsert(List<T> list, int buffer);

    List<T> findSQL(String sql) throws Exception;

    List<T> findSQL(String sql, int currentPage, int pageSize) throws Exception;

    List<T> findSQL(String sql, Map<String, Object> params) throws Exception;

    List<T> findSQL(String sql, Map<String, Object> params, int currentPage, int pageSize) throws Exception;

    void insertList(List<T> list) throws Exception;

    void executeHql(String hql) throws Exception;

    void executeHql(String hql, Map<String, Object> params) throws Exception;

    void executeSql(String sql) throws Exception;

    Serializable add(T t) throws Exception;

    void saveOrUpdate(T t) throws Exception;

    void delete(T t) throws Exception;

    /**
     * 根据多个id删除对象
     */
    void deleteObjects(Class<T> clazz, String[] ids) throws Exception;

    /**
     * 改变对象状态
     */
    void updateStatus(Class<T> clazz, String[] ids, String status) throws Exception;

    /**
     * 改变对象状态
     *
     * @param params
     *            自定义hql条件
     */
    void updateStatus(Class<T> clazz, String[] ids, String status, String where, Map<String, Object> params) throws Exception;

    void update(T t) throws Exception;

    /**
     * 获取所有记录
     */
    List<T> getAllObjects(Class<T> clazz) throws Exception;

    /**
     * 根据条件获取记录
     */
    List<T> getObjects(Class<T> clazz, Map<String, Object> params, Map<String, String> conditions, String order) throws Exception;

    /**
     * 分页查询数据
     *
     * @param clazz
     *            类
     * @param params
     *            参数
     * @param conditions
     *            条件
     */
    List<T> getObjcetPaginationMKI(Class<T> clazz, Map<String, Object> params, Map<String, String> conditions, int page, int pageSize, String order) throws Exception;

    /**
     * 分页查询数据
     *
     * @param clazz
     *            类
     * @param params
     *            参数
     * @param conditions
     *            条件
     */
    List<T> getObjcetPagination(Class<T> clazz, Map<String, Object> params, Map<String, String> conditions, int currentPage, int pageSize, String order) throws Exception;

    /**
     * 获取对象总数量
     */
    Long getObjectNum(Class<T> clazz, Map<String, Object> params, Map<String, String> conditions) throws Exception;

    /**
     * 获取对象某字段总和
     */
    Double getObjectSum(Class<T> clazz, String field, Map<String, Object> params, Map<String, String> conditions) throws Exception;

    /**
     * 根据起始条数查询
     *
     * @param clazz
     *            类
     * @param params
     *            参数
     * @param conditions
     *            条件
     * @param first
     *            开始条数
     */
    List<T> getObjcetStart(Class<T> clazz, Map<String, Object> params, Map<String, String> conditions, int first, int rows, String order) throws Exception;

    /**
     * 获取connection，用于手动jdbc操作
     */
    Connection getConnection() throws Exception;
}
