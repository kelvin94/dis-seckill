package com.jyl.authapi.authapi.model;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class AuditingConfig {
    /*
    To enable JPA Auditing, we’ll need to add @EnableJpaAuditing annotation to our main class or any other configuration classes.
     */
}
