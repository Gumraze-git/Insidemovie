package com.insidemovie.backend;

import com.insidemovie.backend.api.constant.Authority;
import com.insidemovie.backend.api.auth.service.DemoSessionService;
import com.insidemovie.backend.api.member.dto.MemberLoginResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "demo.accounts.enabled=true")
class DemoAuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DemoSessionService demoSessionService;

    @Test
    void shouldExposeDemoAccountsList() throws Exception {
        mockMvc.perform(get("/api/v1/auth/demo-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts.length()").value(35))
                .andExpect(jsonPath("$.accounts[0].accountKey").value("onboarding-01"))
                .andExpect(jsonPath("$.accounts[0].category").value("ONBOARDING"));
    }

    @Test
    void shouldCreateDemoSessionWithCookies() throws Exception {
        MemberLoginResponseDto loginResponse = MemberLoginResponseDto.builder()
                .accessToken("demo-access-token")
                .refreshToken("demo-refresh-token")
                .authority(Authority.ROLE_USER)
                .build();
        given(demoSessionService.createSessionByAccountKey(eq("onboarding-01")))
                .willReturn(loginResponse);

        mockMvc.perform(post("/api/v1/auth/demo-sessions")
                        .contentType("application/json")
                        .content("{\"accountKey\":\"onboarding-01\"}"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Set-Cookie",
                        org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("ACCESS_TOKEN=demo-access-token"))))
                .andExpect(header().stringValues("Set-Cookie",
                        org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("REFRESH_TOKEN=demo-refresh-token"))))
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.authority").value("ROLE_USER"));
    }

    @Test
    void shouldReturnProblemDetailWhenAccountKeyMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-sessions")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_REQUEST_MISSING_EXCEPTION"))
                .andExpect(jsonPath("$.errors[0].field").value("accountKey"));
    }
}
