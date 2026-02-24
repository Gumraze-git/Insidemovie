package com.insidemovie.backend.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DemoAccountListResponse {
    private List<DemoAccountOptionResponse> accounts;
}
