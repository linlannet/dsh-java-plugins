package net.linlan.dsh.oceanbase.model;

public record ConnectionResponse(
        String id,
        String name,
        String host,
        int port,
        String database,
        String username,
        boolean hasPassword
) {
}
