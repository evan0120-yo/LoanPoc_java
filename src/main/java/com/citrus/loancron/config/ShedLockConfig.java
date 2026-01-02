package com.citrus.loancron.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

/**
 * ShedLock 分散式鎖配置
 * 
 * 確保多台 Server 同時運行時，排程任務只在一台執行
 * 
 * POC 用 DB 版本，生產環境可換成 Redis：
 * 1. 換依賴：shedlock-provider-jdbc-template → shedlock-provider-redis-spring
 * 2. 換 Bean：JdbcTemplateLockProvider → RedisLockProvider
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime() // 使用 DB 時間，避免各 Server 時間不同步
                        .build());
    }
}
