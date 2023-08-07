package com.lib.citylib.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.lib.citylib.camTra.utils.ReplaceTableInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.swing.*;

@Configuration
public class MybatisPlusConfig {

    @Resource
    private ReplaceTableInterceptor replaceTableInterceptor;
    /**
     * 新增分页拦截器，并设置数据库类型为mysql
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setOverflow(true);
//        interceptor.addInnerInterceptor(replaceTableInterceptor);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

}
