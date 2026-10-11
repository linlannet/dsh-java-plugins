package net.linlan.dsh.dameng.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dsh.dameng")
public record DamengAdminProperties(
        String dataDirectory,
        int queryTimeoutSeconds,
        int maxRows,
        Harness harness
) {
    public record Harness(String baseUrl) {
    }
}
