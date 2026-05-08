package com.assetvault.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring configuration for jpa auditing.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
