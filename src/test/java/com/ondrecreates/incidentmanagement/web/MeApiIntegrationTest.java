package com.ondrecreates.incidentmanagement.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ondrecreates.incidentmanagement.domain.AppUserRole;
import com.ondrecreates.incidentmanagement.domain.Role;
import com.ondrecreates.incidentmanagement.repository.AppUserRoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * RBAC acceptance: role comes from our own app_user_role table (see
 * AuthorizationService), not a JWT claim -- anyone with no row defaults to MEMBER.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MeApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRoleRepository roleRepository;

    @Test
    void userWithNoRoleRowDefaultsToMember() throws Exception {
        mockMvc.perform(get("/api/v1/me").with(jwt().jwt(builder -> builder.subject("nobody@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nobody@example.com"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void userWithAdminRoleRowReturnsAdmin() throws Exception {
        roleRepository.save(new AppUserRole("admin@example.com", Role.ADMIN));

        mockMvc.perform(get("/api/v1/me").with(jwt().jwt(builder -> builder.subject("admin@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
