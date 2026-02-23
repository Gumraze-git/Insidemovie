package com.insidemovie.backend.api.match.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteCreateRequest {
    @NotNull
    private Long movieId;
}
