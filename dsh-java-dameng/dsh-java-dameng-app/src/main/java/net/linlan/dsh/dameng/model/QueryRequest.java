package net.linlan.dsh.dameng.model;

import jakarta.validation.constraints.NotBlank;

public record QueryRequest(@NotBlank String sql, boolean allowWrite) {
}
