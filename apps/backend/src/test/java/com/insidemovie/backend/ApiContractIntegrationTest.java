package com.insidemovie.backend;

import com.insidemovie.backend.api.member.dto.MemberSignupRequestDto;
import com.insidemovie.backend.api.member.service.MemberRegistrationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberRegistrationService memberRegistrationService;

    @Test
    void signupShouldReturn201WithLocation() throws Exception {
        given(memberRegistrationService.signup(ArgumentMatchers.any(MemberSignupRequestDto.class)))
                .willReturn(Map.of("userId", 123L));

        String body = """
                {
                  "email": "test@example.com",
                  "password": "Abcd1234!",
                  "checkedPassword": "Abcd1234!",
                  "nickname": "tester"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/users/123")))
                .andExpect(jsonPath("$.userId").value(123));
    }

    @Test
    void unauthorizedShouldReturnProblemDetail() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.timestamp", endsWith("Z")))
                .andExpect(jsonPath("$.traceId").isString());
    }

    @Test
    void validationFailureShouldReturnProblemDetailWithErrors() throws Exception {
        mockMvc.perform(post("/api/v1/auth/providers/kakao/token-exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_REQUEST_MISSING_EXCEPTION"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("code"));
    }
}
