package com.insidemovie.backend.api.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMemberStatusUpdateRequest {
    @NotNull
    private Boolean banned;
}
