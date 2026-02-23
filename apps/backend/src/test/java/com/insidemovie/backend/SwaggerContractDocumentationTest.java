package com.insidemovie.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerContractDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldExposeCookieSecuritySchemeAndProblemSchemas() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.type").value("apiKey"))
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.in").value("cookie"))
                .andExpect(jsonPath("$.components.schemas.ProblemDetailContract").exists())
                .andExpect(jsonPath("$.components.schemas.ValidationErrorItemContract").exists())
                .andExpect(jsonPath("$.components.responses.Problem401").exists());
    }

    @Test
    void shouldDocumentLocationHeaderForCreatedApis() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/members'].post.responses['201'].headers.Location").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reviews/{reviewId}/likes/me'].put.responses['201'].headers.Location").exists());
    }

    @Test
    void shouldSeparateSecurityBetweenPublicAndProtectedApis() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/movies/popular'].get.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/members/me'].get.security[0].cookieAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/members/me'].get.responses['401'].$ref")
                        .value("#/components/responses/Problem401"));
    }
}

