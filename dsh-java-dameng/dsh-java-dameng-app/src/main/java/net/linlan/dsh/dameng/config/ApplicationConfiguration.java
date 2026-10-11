package net.linlan.dsh.dameng.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DamengAdminProperties.class)
public class ApplicationConfiguration {
}
