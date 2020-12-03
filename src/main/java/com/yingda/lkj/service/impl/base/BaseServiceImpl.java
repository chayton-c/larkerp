package com.yingda.lkj.service.impl.base;

import com.yingda.lkj.dao.BaseDao;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.sql.Connection;
import java.util.*;

/**
 * @author hood  2019/12/13
 */
@SuppressWarnings("unused")
@Service("baseService")
public class BaseServiceImpl<T> implements BaseService<T> {

    private BaseDao<T> baseDao;

    @Autowired
    public void setBaseDao(BaseDao<T> baseDao) {
        this.baseDao = baseDao;
    }

    public T get(Class<T> clazz, Serializable id) {
        return baseDao.get(clazz, id);
    }

    @Override
    public T get(String hql) {
        return baseDao.get(hql);
    }

    @Override
    public T get(String hql, Map<String, Object> params) {
        return baseDao.get(hql, params);
    }

    public List<T> find(String hql) {
        return baseDao.find(hql);
    }

    public List<T> find(String hql, Map<String, Object> params) {
        return baseDao.find(hql, params);
    }

    @Override
    public List<T> find(String hql, int page, int rows) {
        return baseDao.find(hql, page, rows);
    }

    @Override
    public List<T> find(String hql, Map<String, Object> params, int page, int rows) {
        return baseDao.find(hql, params, page, rows);
    }

    public List<T> findSQL(String sql) {
        return baseDao.findSQL(sql);
    }

    public List<T> findSQL(String sql, Map<String, Object> params) {
        return baseDao.findSQL(sql, params);
    }

    public List<T> findSQL(String sql, Map<String, Object> params, Class<T> clazz) {
        return baseDao.findSQL(sql, params, clazz);
    }

    public List<T> findSQL(String sql, Map<String, Object> params, Class<T> clazz, int page, int rows) {
        return baseDao.findSQL(sql, params, clazz, page, rows);
    }

    @Override
    public List<T> findSQL(String sql, int page, int rows) {
        return baseDao.findSQL(sql, page, rows);
    }

    @Override
    public List<T> findSQL(String sql, Map<String, Object> params, int page, int rows) {
        return baseDao.findSQL(sql, params, page, rows);
    }

    @Override
    public void insertList(List<T> list) {
        if (null != list && list.size() > 0) {
            for (T t : list) {
                baseDao.save(t);
            }
        }
    }

    public void executeHql(String hql) {
        baseDao.executeHql(hql);
    }

    public void executeHql(String hql, Map<String, Object> params) {
        baseDao.executeHql(hql, params);
    }

    public void executeSql(String sql) throws Exception {
        baseDao.executeSql(sql);
    }

    public Serializable add(T t) {
        return baseDao.save(t);
    }

    @Override
    public void saveOrUpdate(T t) {
        baseDao.saveOrUpdate(t);
    }

    @Override
    public void bulkInsert(List<T> list) {
        baseDao.bulkInsert(list);
    }

    @Override
    public void bulkInsert(List<T> list, int buffer) {
        baseDao.bulkInsert(list, buffer);
    }

    public void delete(T t) {
        baseDao.delete(t);
    }

    public void deleteObjects(Class<T> clazz, String[] ids) {
        if (null != ids) {
            String hql = "delete from " + clazz.getSimpleName() + " where ";
            Map<String, Object> params = new HashMap<>();
            hql = getHql(ids, hql, params);
            baseDao.executeHql(hql, params);
        }
    }

    private String getHql(String[] ids, String hql, Map<String, Object> params) {
        StringBuilder hqlBuilder = new StringBuilder(hql);
        for (int x = 0; x < ids.length; x++) {
            String id = ids[x];
            if (x != 0) {
                hqlBuilder.append(" or ");
            }
            hqlBuilder.append("id=:id").append(x);
            params.put("id" + x, id);
        }
        hql = hqlBuilder.toString();
        return hql;
    }

    public void updateStatus(Class<T> clazz, String[] ids, String status) {
        updateStatus(clazz, ids, status, null, null);
    }

    @Override
    public void updateStatus(Class<T> clazz, String[] ids, String status, String where, Map<String, Object> params) {
        if (null != ids && ids.length > 0) {
            String hql = "update " + clazz.getName() + " set status=:p1 where (";
            Map<String, Object> params1 = new HashMap<>();
            params1.put("p1", status);
            hql = getHql(ids, hql, params1);
            hql += ")";
            if (StringUtils.isNotEmpty(where)) {
                hql += " and (" + where + ")";
                params1.putAll(params);
            }

            baseDao.executeHql(hql, params1);
        }
    }

    public void update(T t) {
        baseDao.update(t);
    }

    public List<T> getAllObjects(Class<T> clazz) {
        String hql = "from " + clazz.getName();
        return baseDao.find(hql);
    }

    public List<T> getObjects(Class<T> clazz, Map<String, Object> params, Map<String, String> conditions, String order) {
        List<T> objects;
        Map<String, Object> vparams = new HashMap<>();
        StringBuilder hql = new StringBuilder("from " + clazz.getName() + " where 1=1");
        if (null != params && params.size() > 0) {
            for (String key : params.keySet()) {
                String condition = conditions.get(key);
                Object value = params.get(key);
                String valueKey = key.substring(key.indexOf(".") + 1).replace("#", "");
                if (key.contains("#")) {
                    key = key.substring(0, key.indexOf("#"));
                }
                if (null == value) {
                    hql.append(" and ").append(key).append(" ").append(condition).append(" null");
                } else {
                    hql.append(" and ").append(key).append(" ").append(condition).append(":").append(valueKey);
                    vparams.put(valueKey, value);
                }
            }
            if (null != order && order.contains("order")) {
                hql.append(" ").append(order);
            }

            objects = baseDao.find(hql.toString(), vparams);
        } else {
            if (null != order && order.contains("order")) {
                hql.append(order);
            }
            objects = baseDao.find(hql.toString());
        }

        return objects;
    }

    public List<T> getObjcetPagination(Class<T> clazz, Map<String, Object> params, Map<String, String> conditions, int page, int rows, String order) {
        List<T> objects;
        Map<String, Object> vparams = new HashMap<>();
        StringBuilder hql = new StringBuilder("from " + clazz.getName() + " where 1=1");
        if (null != params && params.size() > 0) {
            for (String key : params.keySet()) {
                String condition = conditions.get(key);
                Object value = params.get(key);
                String valueKey = key.substring(key.indexOf(".") + 1).replace("#", "");
                if (key.contains("#")) {
                    key = key.substring(0, key.indexOf("#"));
                }
                if (null == value) {
                    hql.append(" and ").append(key).append(" ").append(condition).append(" null");
                } else {
                    hql.append(" and ").append(key).append(" ").append(condition).append(":").append(valueKey);
                    vparams.put(valueKey, value);
                }
            }
            if (null != order && order.contains("order")) {
                hql.append(" ").append(order);
            }

            objects = baseDao.find(hql.toString(), vparams, page, rows);
        } else {
            if (null != order && order.contains("order")) {
                hql.append(" ").append(order);
            }
            objects = baseDao.find(hql.toString(), page, rows);
        }
        return objects;
    }

    @Override
    public List<T> getObjcetPaginationMKI(Class<T> clazz, Map<String, Object> params, Map<String, String> conditions, int page, int rows, String order) {
        List<T> objects;
        Map<String, Object> vparams = new HashMap<>();
        StringBuilder hql = new StringBuilder("from " + clazz.getName() + " where 1=1");
        if (null != params && params.size() > 0) {
            for (String rawKey : params.keySet()) {
                String key;
                String condition = conditions.get(rawKey);
                if (rawKey.contains("(") || rawKey.contains(")"))
                    key = rawKey.substring(1);
                else
                    key = rawKey;


                Object value = params.get(rawKey);
                String valueKey = key.substring(key.indexOf(".") + 1).replace("#", "");
                if (key.contains("#")) {
                    key = key.substring(0, key.indexOf("#"));
                }
                if (null == value) {
                    hql.append(" and ").append(key).append(" ").append(condition).append(" null");
                } else {
                    if (rawKey.contains("(")) {
                        hql.append(" and ( ").append(key).append(" ").append(condition).append(":").append(valueKey);
                        vparams.put(key, value);

                    }

                    else if (rawKey.contains(")")) {
                        hql.append(" or ").append(key).append(" ").append(condition).append(":").append(valueKey).append(" )");
                        vparams.put(key, value);
                    }

                    else {
                        hql.append(" and ").append(key).append(" ").append(condition).append(":").append(valueKey);
                        vparams.put(valueKey, value);
                    }
                }
            }
            if (null != order && order.contains("order")) {
                hql.append(" ").append(order);
            }

            objects = baseDao.find(hql.toString(), vparams, page, rows);
        } else {
            if (null != order && order.contains("order")) {
                hql.append(" ").append(order);
            }
            objects = baseDao.find(hql.toString(), page, rows);
        }
        return objects;
    }

    public Long getObjectNum(Class<T> clazz, Map<String, Object> params, Map<String, String> conditions) {
        StringBuilder hql = new StringBuilder("select count(*) from " + clazz.getName() + " where 1=1");
        Map<String, Object> vparams = new HashMap<>();
        Long objNum;
        if (null != params && params.size() > 0) {
            for (String key : params.keySet()) {
                String condition = conditions.get(key);
                Object value = params.get(key);
                String valueKey = key.substring(key.indexOf(".") + 1).replace("#", "");
                if (key.contains("#")) {
                    key = key.substring(0, key.indexOf("#"));
                }
                if (null == value) {
                    hql.append(" and ").append(key).append(" ").append(condition).append(" null");
                } else {
                    hql.append(" and ").append(key).append(" ").append(condition).append(":").append(valueKey);
                    vparams.put(valueKey, value);
                }
            }
            objNum = baseDao.getCount(hql.toString(), vparams);
        } else {
            objNum = baseDao.getCount(hql.toString());
        }
        return objNum;
    }

    public Long getObjectNumMKI(Class<T> clazz, Map<String, Object> params, Map<String, String> conditions) {
        StringBuilder hql = new StringBuilder("select count(*) from " + clazz.getName() + " where 1=1");
        Map<String, Object> vparams = new HashMap<>();
        Long objNum;
        if (null != params && params.size() > 0) {
            for (String rawKey : params.keySet()) {
                String key;
                String condition = conditions.get(rawKey);
                if (rawKey.contains("(") || rawKey.contains(")"))
                    key = rawKey.substring(1);
                else
                    key = rawKey;


                Object value = params.get(rawKey);
                String valueKey = key.substring(key.indexOf(".") + 1).replace("#", "");
                if (key.contains("#")) {
                    key = key.substring(0, key.indexOf("#"));
                }
                if (null == value) {
                    hql.append(" and ").append(key).append(" ").append(condition).append(" null");
                } else {
                    if (rawKey.contains("(")) {
                        hql.append(" and ( ").append(key).append(" ").append(condition).append(":").append(valueKey);
                        vparams.put(key, value);
                    }

                    else if (rawKey.contains(")")) {
                        hql.append(" or ").append(key).append(" ").append(condition).append(":").append(valueKey).append(" )");
                        vparams.put(key, value);
                    }

                    else {
                        hql.append(" and ").append(key).append(" ").append(condition).append(":").append(valueKey);
                        vparams.put(valueKey, value);
                    }
                }

            }
            objNum = baseDao.getCount(hql.toString(), vparams);
        } else {
            objNum = baseDao.getCount(hql.toString());
        }
        return objNum;
    }

    public Double getObjectSum(Class<T> clazz, String field, Map<String, Object> params, Map<String, String> conditions) {
        StringBuilder hql = new StringBuilder("select sum(" + field + ") from " + clazz.getName() + " where 1=1");
        Map<String, Object> vparams = new HashMap<>();
        T objNum;
        if (null != params && params.size() > 0) {
            for (String key : params.keySet()) {
                String condition = conditions.get(key);
                Object value = params.get(key);
                String valueKey = key.substring(key.indexOf(".") + 1).replace("#", "");
                if (key.contains("#")) {
                    key = key.substring(0, key.indexOf("#"));
                }
                if (null == value) {
                    hql.append(" and ").append(key).append(" ").append(condition).append(" null");
                } else {
                    hql.append(" and ").append(key).append(" ").append(condition).append(":").append(valueKey);
                    vparams.put(valueKey, value);
                }
            }
            objNum = baseDao.get(hql.toString(), vparams);
        } else {
            objNum = baseDao.get(hql.toString());
        }
        return StringUtils.isEmpty(objNum) ? 0 : Double.parseDouble(objNum + "");
    }

    public List<T> getObjcetStart(Class<T> clazz, Map<String, Object> params, Map<String, String> conditions, int start, int rows, String order) {
        List<T> objects;
        Map<String, Object> vparams = new HashMap<>();
        StringBuilder hql = new StringBuilder("from " + clazz.getName() + " where 1=1");
        if (null != params && params.size() > 0) {
            for (String key : params.keySet()) {
                String condition = conditions.get(key);
                Object value = params.get(key);
                String valueKey = key.substring(key.indexOf(".") + 1).replace("#", "");
                if (key.contains("#")) {
                    key = key.substring(0, key.indexOf("#"));
                }
                if (null == value) {
                    hql.append(" and ").append(key).append(" ").append(condition).append(" null");
                } else {
                    hql.append(" and ").append(key).append(" ").append(condition).append(":").append(valueKey);
                    vparams.put(valueKey, value);
                }
            }
            if (null != order && order.contains("order")) {
                hql.append(" ").append(order);
            }

            objects = baseDao.findByStart(hql.toString(), vparams, start, rows);
        } else {
            if (null != order && order.contains("order")) {
                hql.append(" ").append(order);
            }
            objects = baseDao.findByStart(hql.toString(), start, rows);
        }

        return objects;
    }

    @Override
    public Connection getConnection() throws Exception {
        return baseDao.getConnection();
    }


}

