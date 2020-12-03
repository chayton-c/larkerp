package com.yingda.lkj.dao;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @author hood  2019/12/13
 */
@SuppressWarnings("unused")
public interface BaseDao<T> {

    /**
     * 保存方法
     */
    Serializable save(T t);

    /**
     * 更新对象
     */
    void update(T t);

    /**
     * 更新对象
     */
    void flush();

    /**
     * 删除对象
     */
    void delete(T t);

    /**
     * 插入或更新对象
     */
    void saveOrUpdate(T t);

    /**
     * 获得一个对象
     */
    T get(Class<T> c, Serializable id);

    /**
     * 根据hql查询 无参数
     */
    T get(String hql);

    /**
     * 批量插入, 10000条大概四秒，嫌慢去拼sql
     */
    void bulkInsert(List<T> list);

    /**
     * 批量插入, 手动设置缓冲区的插入数量，即每buffer条数据提交一次, 10000条大概四秒，嫌慢去拼sql
     */
    void bulkInsert(List<T> list, int buffer);

    /**
     * 根据hql查询 有参数 例如 ： from User u where u.name = :name and u.pwd = :pwd
     */
    T get(String hql, Map<String, Object> params);

    /**
     * 根据hql获取结果数量
     */
    Long getCount(String hql);

    /**
     * 获取结果数量，带参数
     */
    Long getCount(String hql, Map<String, Object> params);

    /**
     * 查询集合
     */
    List<T> find(String hql);

    /**
     * 查询集合，带参数
     */
    List<T> find(String hql, Map<String, Object> params);

    /**
     * 分页查询，无参数
     */
    List<T> find(String hql, int currentPage, int pageSize);

    /**
     * 分页查询，有参数
     */
    List<T> find(String hql, Map<String, Object> params, int currentPage, int pageSize);

    /**
     * 根据起始条数查询,无参数
     */
    List<T> findByStart(String hql, int first, int pageSize);

    /**
     * 根据起始条数查询
     */
    List<T> findByStart(String hql, Map<String, Object> params, int first, int pageSize);

    /**
     * 查询sql
     */
    List<T> findSQL(String sql);

    /**
     * 根据参数查询sql
     */
    List<T> findSQL(String sql, Map<String, Object> params);


    /**
     * 根据参数查询sql
     */
    List<T> findSQL(String sql, Map<String, Object> params, Class<T> clazz);

    /**
     * 根据sql查询
     */
    T getSQL(String sql, Map<String, Object> params, Class<T> clazz);

    /**
     * 根据sql分页查询
     */
    List<T> findSQL(String sql, Map<String, Object> params, Class<T> clazz, int currentPage, int pageSize);

    List<T> findSQL(String sql, int currentPage, int pageSize);

    List<Map<String, Object>> findSQLtoMap(String sql, int currentPage, int pageSize);

    /**
     * 分页查询 带参数的sql
     */
    List<T> findSQL(String sql, Map<String, Object> params, int currentPage, int pageSize);

    /**
     * 执行无参hql
     */
    int executeHql(String hql);

    /**
     * 执行有参hql
     */
    int executeHql(String hql, Map<String, Object> params);

    /**
     * 插入操作日志
     */
    void setLog(T t, String operateType);

    /**
     * 获取connection 用户手动jdbc操作
     */
    Connection getConnection() throws SQLException;

    /**
     * 执行无参sql
     */
    int executeSql(String sql) throws SQLException;

    /**
     * 执行有参sql
     */
    int executeSql(String sql, Map<String, Object> params) throws SQLException;

}
