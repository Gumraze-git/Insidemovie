package com.insidemovie.backend.api.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthSessionResponse {
    private String authority;
    private boolean authenticated;
    private boolean refreshed;
}
