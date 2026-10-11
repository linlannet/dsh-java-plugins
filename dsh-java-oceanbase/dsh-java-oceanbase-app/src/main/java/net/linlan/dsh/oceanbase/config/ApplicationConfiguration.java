package net.linlan.dsh.oceanbase.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OceanBaseAdminProperties.class)
public class ApplicationConfiguration {
}
