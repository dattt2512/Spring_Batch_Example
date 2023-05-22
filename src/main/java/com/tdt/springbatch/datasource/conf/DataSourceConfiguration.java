package com.tdt.springbatch.datasource.conf;

import com.tdt.springbatch.datasource.context.DatabaseEnvironment;
import com.tdt.springbatch.datasource.routing.MasterSlaveRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfiguration {

    @Value("${spring.datasource.url}")
    private String mstUrl;

    @Value("${spring.datasource.username}")
    private String mstUsername;

    @Value("${spring.datasource.password}")
    private String mstPassword;

    @Value("${jdbc.slave.url}")
    private String slaveUrl;

    @Value("${jdbc.slave.username}")
    private String slaveUsername;

    @Value("${jdbc.slave.password}")
    private String slavePassword;

    @Bean
    public DataSource dataSource(){
        MasterSlaveRoutingDataSource masterSlaveRoutingDataSource = new MasterSlaveRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DatabaseEnvironment.UPDATABLE, masterDataSource());
        targetDataSources.put(DatabaseEnvironment.READONLY, slaveDataSource());
        masterSlaveRoutingDataSource.setTargetDataSources(targetDataSources);

        // Set as all transaction point to master
        masterSlaveRoutingDataSource.setDefaultTargetDataSource(masterDataSource());
        return masterSlaveRoutingDataSource;
    }

    public DataSource slaveDataSource() {

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

    public DataSource masterDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(mstUrl);
        hikariDataSource.setUsername(mstUsername);
        hikariDataSource.setPassword(mstPassword);
        hikariDataSource.setMaximumPoolSize(100);
        hikariDataSource.setConnectionTimeout(3000000);
        hikariDataSource.setLeakDetectionThreshold(3000000);
        hikariDataSource.setMaxLifetime(3000000);
        return hikariDataSource;
    }
}
