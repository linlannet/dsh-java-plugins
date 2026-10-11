package net.linlan.dsh.oceanbase.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dsh.oceanbase")
public record OceanBaseAdminProperties(
        String dataDirectory,
        int queryTimeoutSeconds,
        int maxRows,
        Harness harness
) {
    public record Harness(String baseUrl) {
    }
}
