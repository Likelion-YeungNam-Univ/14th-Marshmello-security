package com.example.daveyauth;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityFlowTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void authorizationRequestUsesCodeStateNonceAndPkce() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/oidc"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", containsString("response_type=code")))
            .andExpect(header().string("Location", containsString("state=")))
            .andExpect(header().string("Location", containsString("nonce=")))
            .andExpect(header().string("Location", containsString("code_challenge=")))
            .andExpect(header().string("Location", containsString("code_challenge_method=S256")));
    }

    @Test
    void privateApiRejectsAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void modelGateAllowsExactVerifiedEmail() throws Exception {
        mockMvc.perform(get("/api/model-gate").with(oidcLogin()
                .idToken(token -> token
                    .subject("subject-1")
                    .claim("email", "allowed@example.com")
                    .claim("email_verified", true))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authorized").value(true))
            .andExpect(jsonPath("$.message").value("MODEL_GATE_PASSED"));
    }

    @Test
    void modelGateAllowsVerifiedEmailDomain() throws Exception {
        mockMvc.perform(get("/api/model-gate").with(oidcLogin()
                .idToken(token -> token
                    .subject("subject-2")
                    .claim("email", "person@trusted.example")
                    .claim("email_verified", true))))
            .andExpect(status().isOk());
    }

    @Test
    void modelGateAllowsExplicitSubject() throws Exception {
        mockMvc.perform(get("/api/model-gate").with(oidcLogin()
                .idToken(token -> token
                    .subject("subject-allowlisted")
                    .claim("email", "other@example.net")
                    .claim("email_verified", true))))
            .andExpect(status().isOk());
    }

    @Test
    void modelGateAllowsExplicitSubjectWithOpenidOnlyClaims() throws Exception {
        mockMvc.perform(get("/api/model-gate").with(oidcLogin()
                .idToken(token -> token
                    .subject("subject-allowlisted"))))
            .andExpect(status().isOk());
    }

    @Test
    void modelGateRejectsUnverifiedEmail() throws Exception {
        mockMvc.perform(get("/api/model-gate").with(oidcLogin()
                .idToken(token -> token
                    .subject("subject-1")
                    .claim("email", "allowed@example.com")
                    .claim("email_verified", false))))
            .andExpect(status().isForbidden());
    }

    @Test
    void modelGateRejectsUserOutsideAllowList() throws Exception {
        mockMvc.perform(get("/api/model-gate").with(oidcLogin()
                .idToken(token -> token
                    .subject("unknown-subject")
                    .claim("email", "unknown@example.net")
                    .claim("email_verified", true))))
            .andExpect(status().isForbidden());
    }
}
