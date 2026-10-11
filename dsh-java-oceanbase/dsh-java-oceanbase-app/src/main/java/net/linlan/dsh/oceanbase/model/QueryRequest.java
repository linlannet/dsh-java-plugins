package net.linlan.dsh.oceanbase.model;

import jakarta.validation.constraints.NotBlank;

public record QueryRequest(@NotBlank String sql, boolean allowWrite) {
}
