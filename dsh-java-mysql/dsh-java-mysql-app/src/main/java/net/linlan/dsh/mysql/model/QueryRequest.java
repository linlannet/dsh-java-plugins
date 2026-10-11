package net.linlan.dsh.mysql.model;

import jakarta.validation.constraints.NotBlank;

public record QueryRequest(@NotBlank String sql, boolean allowWrite) {
}
