package com.lib.citylib.camTra.utils;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;


import java.util.*;


/**
 * @description: 动态替换表名拦截器
 * @author: hinotoyk
 * @created: 2022/04/19
 */

//method = "query"拦截select方法、而method = "update"则能拦截insert、update、delete的方法

@Component
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
})
public class ReplaceTableInterceptor implements Interceptor {
    
    private Map<String,String> tableMap;

    public ReplaceTableInterceptor() {
        this.tableMap = new LinkedHashMap<>();
        this.tableMap.put("camtrajectory", "camtrajectory");
    }

    public void setTableName(String tableName){
        this.tableMap.replace("camtrajectory", tableName);
    }
    public String getTableName(){
        return this.tableMap.get("camtrajectory");
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object[] args = invocation.getArgs();
        //获取MappedStatement对象
        MappedStatement ms = (MappedStatement) args[0];

        //获取传入sql语句的参数对象
        Object parameterObject = args[1];
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        //获取到拥有占位符的sql语句
        String sql = boundSql.getSql();
        System.out.println("拦截前sql :" + sql);

        //判断是否需要替换表名
        if(isReplaceTableName(sql)){
            for(Map.Entry<String, String> entry : tableMap.entrySet()){
                sql = sql.replace(entry.getKey(),entry.getValue());
            }
            System.out.println("拦截后sql :" + sql);

            //重新生成一个BoundSql对象
            BoundSql bs = new BoundSql(ms.getConfiguration(),sql,boundSql.getParameterMappings(),parameterObject);

            //重新生成一个MappedStatement对象
            MappedStatement newMs = copyMappedStatement(ms, new BoundSqlSqlSource(bs));

            //赋回给实际执行方法所需的参数中
            args[0] = newMs;
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    /***
     * 判断是否需要替换表名
     * @param sql
     * @return
     */
    private boolean isReplaceTableName(String sql){
        for(String tableName : tableMap.keySet()){
            if(sql.contains(tableName)){
                return true;
            }
        }
        return false;
    }

    /***
     * 复制一个新的MappedStatement
     * @param ms
     * @param newSqlSource
     * @return
     */
    private MappedStatement copyMappedStatement (MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());

        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length > 0) {
            builder.keyProperty(String.join(",",ms.getKeyProperties()));
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    /***
     * MappedStatement构造器接受的是SqlSource
     * 实现SqlSource接口，将BoundSql封装进去
     */
    public static class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;
        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }
        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }


}
