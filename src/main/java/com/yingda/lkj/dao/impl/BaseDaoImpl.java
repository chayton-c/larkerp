package com.yingda.lkj.dao.impl;

import com.yingda.lkj.dao.BaseDao;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @author hood  2019/12/13
 */
@Primary
@Repository("baseDao")
public class BaseDaoImpl<T> implements BaseDao<T> {

    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public Serializable save(T t) {
        Serializable s = this.getCurrentSession().save(this.getCurrentSession().merge(t));
        setLog(t, "新增");
        return s;
    }

    public void update(T t) {
        this.getCurrentSession().update(this.getCurrentSession().merge(t));
        setLog(t, "更新");
    }

    @Override
    public void flush() {
        Session session = this.getCurrentSession();
        session.flush();
    }

    public void delete(T t) {
        this.getCurrentSession().delete(t);
        setLog(t, "删除");
    }

    public void saveOrUpdate(T t) {
        this.getCurrentSession().saveOrUpdate(this.getCurrentSession().merge(t));
        setLog(t, "新增或更新");
    }


    public void bulkInsert(List<T> list) {
        bulkInsert(list, 10000);
    }

    public void bulkInsert(List<T> list, int buffer) {
        Session session = this.getCurrentSession();
        int times = 0;
        for (T t : list) {
            session.saveOrUpdate(t);
            times++;
            if (times % buffer == 0) {
                session.flush();
                session.clear();
            }
        }
        session.flush();
        session.clear();
    }

    public T get(Class<T> c, Serializable id) {
        return this.getCurrentSession().get(c, id);
    }

    public T get(String hql) {
        List<T> ls = find(hql);
        if (null != ls && ls.size() > 0)
            return ls.get(0);

        return null;
    }

    public T get(String hql, Map<String, Object> params) {
        List<T> ls = find(hql, params);
        if (null != ls && ls.size() > 0)
            return ls.get(0);

        return null;
    }

    public Long getCount(String hql) {
        return (Long) this.getCurrentSession().createQuery(hql).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public Long getCount(String hql, Map<String, Object> params) {
        hql = pretreatment(hql, params);
        if (hql == null)
            return 0L;

        Query<T> q = this.getCurrentSession().createQuery(hql);
        setParam(q, params);
        return (Long) q.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<T> find(String hql) {
        Query<T> q = this.getCurrentSession().createQuery(hql);
        return q.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> find(String hql, Map<String, Object> params) {
        hql = pretreatment(hql, params);
        if (hql == null)
            return new ArrayList<>();

        Query<T> q = this.getCurrentSession().createQuery(hql);
        setParam(q, params);
        return q.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> find(String hql, int page, int rows) {
        if (page < 1) {
            page = 1;
        }
        if (rows < 1) {
            rows = 10;
        }
        Query<T> q = this.getCurrentSession().createQuery(hql);
        return q.setFirstResult((page - 1) * rows).setMaxResults(rows).list();
    }

    @SuppressWarnings("unchecked")
    public List<T> find(String hql, Map<String, Object> params, int page, int rows) {
        hql = pretreatment(hql, params);
        if (hql == null)
            return new ArrayList<>();

        if (page < 1) {
            page = 1;
        }
        if (rows < 1) {
            rows = 10;
        }
        Query<T> q = this.getCurrentSession().createQuery(hql);
        setParam(q, params);
        return q.setFirstResult((page - 1) * rows).setMaxResults(rows).list();
    }

    @SuppressWarnings("unchecked")
    public List<T> findSQL(String sql) {
        NativeQuery<T> sq = this.getCurrentSession().createSQLQuery(sql);
        return sq.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> findSQL(String sql, Map<String, Object> params) {
        sql = pretreatment(sql, params);
        if (sql == null)
            return new ArrayList<>();

        NativeQuery<T> q = this.getCurrentSession().createSQLQuery(sql);
        setParam(q, params);
        return q.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> findSQL(String sql, Map<String, Object> params, Class<T> clazz) {
        sql = pretreatment(sql, params);
        if (sql == null)
            return new ArrayList<>();

        NativeQuery<T> q = this.getCurrentSession().createNativeQuery(sql).unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.aliasToBean(clazz));
        setParam(q, params);
        return q.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> findSQL(String sql, int page, int rows) {
        if (page < 1) {
            page = 1;
        }
        if (rows < 1) {
            rows = 10;
        }
        NativeQuery<T> sq = this.getCurrentSession().createSQLQuery(sql);
        return sq.setFirstResult((page - 1) * rows).setMaxResults(rows).list();
    }

    @SuppressWarnings("unchecked")
    public List<T> findSQL(String sql, Map<String, Object> params, int page, int rows) {
        sql = pretreatment(sql, params);
        if (sql == null)
            return new ArrayList<>();

        if (page < 1) {
            page = 1;
        }
        if (rows < 1) {
            rows = 10;
        }
        NativeQuery<T> q = this.getCurrentSession().createSQLQuery(sql);
        setParam(q, params);
        return q.setFirstResult((page - 1) * rows).setMaxResults(rows).list();
    }

    @SuppressWarnings("unchecked")
    public List<T> findSQL(String sql, Map<String, Object> params, Class<T> clazz, int page, int rows) {
        sql = pretreatment(sql, params);
        if (sql == null)
            return new ArrayList<>();

        NativeQuery<T> q = this.getCurrentSession().createNativeQuery(sql).unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.aliasToBean(clazz));
        setParam(q, params);
        return q.setFirstResult((page - 1) * rows).setMaxResults(rows).list();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> findSQLtoMap(String sql, int page, int rows) {
        if (page < 1) {
            page = 1;
        }
        if (rows < 1) {
            rows = 10;
        }

        Query<Map<String, Object>> q =
                this.getCurrentSession().createSQLQuery(sql).unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        return q.setFirstResult((page - 1) * rows).setMaxResults(rows).list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findByStart(String hql, int first, int rows) {
        if (rows < 1) {
            rows = 10;
        }
        Query<T> q = this.getCurrentSession().createQuery(hql);
        return q.setFirstResult(first).setMaxResults(rows).list();
    }

    @SuppressWarnings("unchecked")
    public List<T> findByStart(String hql, Map<String, Object> params, int first, int rows) {
        hql = pretreatment(hql, params);
        if (hql == null)
            return new ArrayList<>();

        if (rows < 1) {
            rows = 10;
        }
        Query<T> q = this.getCurrentSession().createQuery(hql);
        setParam(q, params);
        return q.setFirstResult(first).setMaxResults(rows).list();
    }

    @SuppressWarnings("unchecked")
    public int executeHql(String hql) {
        Query<T> q = this.getCurrentSession().createQuery(hql);
        int ret = q.executeUpdate();
        setExecuteSqlLog(hql, null);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public int executeHql(String hql, Map<String, Object> params) {
        hql = pretreatment(hql, params);
        if (hql == null)
            return 0;

        Query<T> q = this.getCurrentSession().createQuery(hql);
        setParam(q, params);
        int ret = q.executeUpdate();
        setExecuteSqlLog(hql, params);
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int executeSql(String sql) {
        Query<T> q = this.getCurrentSession().createSQLQuery(sql);
        int ret = q.executeUpdate();
        setExecuteSqlLog(sql, null);
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int executeSql(String sql, Map<String, Object> params) {
        sql = pretreatment(sql, params);
        if (sql == null)
            return 0;

        Query<T> q = this.getCurrentSession().createSQLQuery(sql);
        setParam(q, params);
        int ret = q.executeUpdate();
        setExecuteSqlLog(sql, params);
        return ret;
    }

    /**
     * 执行hql语句log
     */
    @SuppressWarnings("unused")
    private void setExecuteSqlLog(String hql, Map<String, Object> params) {

    }

    /**
     * 自动插入操作日志
     */
    public void setLog(T t, String operateType) {

    }

    public T getSQL(String sql, Map<String, Object> params, Class<T> clazz) {
        List<T> ls = findSQL(sql, params, clazz, 1, 1);
        if (ls == null || ls.size() <= 0)
            return null;
        return ls.get(0);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return Objects.requireNonNull(SessionFactoryUtils.getDataSource(sessionFactory)).getConnection();
    }

    private void setParam(Query<T> q, Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                if (params.get(key) instanceof List) {
                    q.setParameterList(key, (List<?>) params.get(key));
                } else if (params.get(key) instanceof String[]) {
                    q.setParameterList(key, (String[]) params.get(key));
                } else {
                    q.setParameter(key, params.get(key));
                }
            }
        }
    }

    private String pretreatment(String hql, Map<String, Object> params) {
        if (params == null || params.size() == 0)
            return hql;

        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value instanceof List && ((List<?>) value).size() == 0)
                return null;

            if ((value instanceof List) || (value instanceof String[])) {
                hql = hql.replace("( :" + key + ")", ":" + key);
                hql = hql.replace("( :" + key + " )", ":" + key);
                hql = hql.replace("(:" + key + " )", ":" + key);
                hql = hql.replace("(:" + key + ")", ":" + key);
                hql = hql.replace(":" + key, "(:" + key + ")");
            }
        }
        return hql;
    }

}
