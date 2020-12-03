package com.yingda.lkj.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.*;

import javax.sql.DataSource;
import java.util.*;

/**
 * @author hood  2019/11/27
 */
@Configuration
public class HibernateConfig {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("config");

    private String jdbcUrl = bundle.getString("jdbc_url");
    private String jdbcUsername = bundle.getString("jdbc_username");
    private String jdbcPassword = bundle.getString("jdbc_password");
    private String validationQuery = bundle.getString("validation_query");
    private String hbm2ddlAuto = bundle.getString("hbm2ddlAuto");
    private String dialect = bundle.getString("dialect");
    private String showSql = bundle.getString("showSql");
    private String formatSql = bundle.getString("formatSql");

    @Bean(destroyMethod = "close")
    public DruidDataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);
        dataSource.setInitialSize(0);
        dataSource.setMaxActive(1000);
        dataSource.setMinIdle(0);
        dataSource.setMaxWait(60000);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setRemoveAbandonedTimeout(1800);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setTestWhileIdle(true);
        return dataSource;
    }


    @Bean
    public LocalSessionFactoryBean hibernateSessionFactory() {

        LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();
        localSessionFactoryBean.setDataSource(dataSource());

        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.setProperty("hibernate.dialect", dialect);
        properties.setProperty("hibernate.show_sql", showSql);
        properties.setProperty("hibernate.format_sql", formatSql);
        properties.setProperty("hibernate.allow_update_outside_transaction", "true");

        localSessionFactoryBean.setHibernateProperties(properties);
        localSessionFactoryBean.setPackagesToScan("com.yingda.lkj.beans.entity");

        return localSessionFactoryBean;
    }

    /**
     * 事务拦截类型
     */
    @Bean("txSource")
    public TransactionAttributeSource transactionAttributeSource() {
        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        /* 当前存在事务就使用当前事务，当前不存在事务就创建一个新的事务 */
        RuleBasedTransactionAttribute requiredTx = new RuleBasedTransactionAttribute(
                TransactionDefinition.PROPAGATION_REQUIRED, Collections.singletonList(new RollbackRuleAttribute(Exception.class))
            );
        requiredTx.setTimeout(5);
        Map<String, TransactionAttribute> txMap = new HashMap<>();
        txMap.put("add*", requiredTx);
        txMap.put("save*", requiredTx);

        txMap.put("init*", requiredTx);
        txMap.put("modify*", requiredTx);
        txMap.put("edit*", requiredTx);
        txMap.put("executeHql*", requiredTx);
        txMap.put("executeSql*", requiredTx);
        txMap.put("insert*", requiredTx);

        txMap.put("repair*", requiredTx);
        txMap.put("send*", requiredTx);
        txMap.put("is*", requiredTx);
        txMap.put("update*", requiredTx);

        txMap.put("remove*", requiredTx);
        txMap.put("delete*", requiredTx);

        txMap.put("get*", requiredTx);
        txMap.put("find*", requiredTx);

        txMap.put("*", requiredTx);

        source.setNameMap(txMap);
        return source;
    }

    /**
     * 切面拦截规则 参数会自动从容器中注入
     */
    @Bean
    public AspectJExpressionPointcutAdvisor pointcutAdvisor(TransactionInterceptor txInterceptor) {
        AspectJExpressionPointcutAdvisor pointcutAdvisor = new AspectJExpressionPointcutAdvisor();
        pointcutAdvisor.setAdvice(txInterceptor);
        pointcutAdvisor.setExpression("execution (* com.yingda.lkj.service..*.*(..))");
        return pointcutAdvisor;
    }

    /**
     * 事务拦截器
     */
    @Bean("txInterceptor")
    TransactionInterceptor getTransactionInterceptor(PlatformTransactionManager tx) {
        return new TransactionInterceptor(tx, transactionAttributeSource());
    }

}
