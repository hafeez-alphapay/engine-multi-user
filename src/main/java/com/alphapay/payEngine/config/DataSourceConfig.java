package com.alphapay.payEngine.config;

import com.alphapay.payEngine.service.impl.SecretsManagerService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class DataSourceConfig {
//    @Value("${hikari.minimum.idle:10}")
//    private int hikariMinimumIdle;
//
//    @Value("${hikari.maximum.pool.size:30}")
//    private int maximumPoolSize;
//
//    @Value("${hikari.connection.timeout:30000}")
//    private long connectionTimeout;
//
//    @Value("${hikari.idle.timeout:300000}")
//    private long idleTimeout;
//
//    @Value("${hikari.max.lifetime:1800000}")
//    private long maxLifetime;
//
//    @Bean
//    public Map<String, String> appSecrets(Environment environment, SecretsManagerService secretsManagerService) {
//        String activeProfile = environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "local";
//        log.info(">>> Loading app secrets for profile: {}", activeProfile);
//
//        if ("production".equals(activeProfile)) {
//            return secretsManagerService.getSecret("engineProdDBSecret");
//        } else if ("development".equals(activeProfile)) {
//            return secretsManagerService.getSecret("engineDevDBSecret");
//        } else if ("local".equals(activeProfile)) {
//            return Map.of(
//                    "db.url", "jdbc:mysql://alphapay-dev-db.cfekikgmqr6c.me-central-1.rds.amazonaws.com:3306/payenginedb?useSSL=false",
//                    "db.username", "admin",
//                    "db.password", "Syb3r2g0"
//            );
//        } else {
//            log.warn(">>> Unknown profile '{}', using empty secrets map", activeProfile);
//            return new HashMap<>();
//        }
//    }
//
//    @Bean
//    @Primary
//    public DataSource dataSource(Map<String, String> appSecrets) {
//        log.info(">>> Creating manual DataSource bean");
//
//        HikariConfig hikariConfig = new HikariConfig();
//        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
//        hikariConfig.setJdbcUrl(appSecrets.get("db.url"));
//        hikariConfig.setUsername(appSecrets.get("db.username"));
//        hikariConfig.setPassword(appSecrets.get("db.password"));
//
//        //  Hikari specific properties
//        hikariConfig.setMinimumIdle(hikariMinimumIdle);
//        hikariConfig.setMaximumPoolSize(maximumPoolSize);
//        hikariConfig.setConnectionTimeout(connectionTimeout);
//        hikariConfig.setIdleTimeout(idleTimeout);
//        hikariConfig.setMaxLifetime(maxLifetime);
//
//        return new HikariDataSource(hikariConfig);
//    }
}