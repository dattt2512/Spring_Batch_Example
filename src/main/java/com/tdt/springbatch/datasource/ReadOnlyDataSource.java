package com.tdt.springbatch.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ReadOnlyDataSource {

    @Value("${jdbc.slave.url}")
    private String slaveUrl;

    @Value("${jdbc.slave.username}")
    private String slaveUsername;

    @Value("${jdbc.slave.password}")
    private String slavePassword;

    public DataSource readOnlyDataSource() {

        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(slaveUrl);
        hikariDataSource.setUsername(slaveUsername);
        hikariDataSource.setPassword(slavePassword);
        hikariDataSource.setMaximumPoolSize(100);
        hikariDataSource.setConnectionTimeout(3000000);
        hikariDataSource.setLeakDetectionThreshold(3000000);
        hikariDataSource.setMaxLifetime(3000000);
        return hikariDataSource;
    }
}
