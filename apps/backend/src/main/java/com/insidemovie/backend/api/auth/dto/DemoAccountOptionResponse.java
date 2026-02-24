package com.insidemovie.backend.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DemoAccountOptionResponse {
    private String accountKey;
    private String label;
    private String category;
}
