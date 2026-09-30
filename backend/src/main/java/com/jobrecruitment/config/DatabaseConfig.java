package com.jobrecruitment.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Dynamic DataSource configuration.
 * Automatically adapts between MySQL, PostgreSQL, and cloud URL formats
 * (e.g., converts Render's raw 'postgres://' or 'postgresql://' connection strings to valid JDBC format).
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String rawUrl;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        String jdbcUrl = rawUrl;
        String user = username;
        String pass = password;

        if (rawUrl != null && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            try {
                URI uri = new URI(rawUrl);
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath();
                if (uri.getUserInfo() != null && (user == null || user.isEmpty())) {
                    String[] userInfo = uri.getUserInfo().split(":");
                    user = userInfo[0];
                    if (userInfo.length > 1) {
                        pass = userInfo[1];
                    }
                }
                config.setDriverClassName("org.postgresql.Driver");
                log.info("Converted cloud postgres URL to JDBC format: {}", jdbcUrl);
            } catch (Exception e) {
                log.warn("Could not parse DB_URL as URI, using raw value: {}", e.getMessage());
            }
        } else if (rawUrl != null && rawUrl.startsWith("jdbc:postgresql:")) {
            config.setDriverClassName("org.postgresql.Driver");
        } else if (rawUrl != null && rawUrl.startsWith("jdbc:mysql:")) {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }

        config.setJdbcUrl(jdbcUrl);
        if (user != null && !user.isEmpty()) {
            config.setUsername(user);
        }
        if (pass != null && !pass.isEmpty()) {
            config.setPassword(pass);
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
