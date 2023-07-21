package com.lib.citylib.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class DruidConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() throws SQLException {
//        Properties properties = new Properties();
//        return new ClickHouseDataSource("jdbc:clickhouse://211.87.232.157:8123",properties);
        return new DruidDataSource();
    }
}
