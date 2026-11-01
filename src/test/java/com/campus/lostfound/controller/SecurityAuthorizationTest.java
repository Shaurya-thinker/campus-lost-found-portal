package com.campus.lostfound.controller;

import com.campus.lostfound.config.SecurityConfig;
import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.ClaimStatus;
import com.campus.lostfound.entity.enums.ItemStatus;
import com.campus.lostfound.entity.enums.ReportType;
import com.campus.lostfound.entity.enums.Role;
import com.campus.lostfound.security.CustomUserDetailsService;
import com.campus.lostfound.security.JwtAuthenticationFilter;
import com.campus.lostfound.security.JwtService;
import com.campus.lostfound.security.RestAccessDeniedHandler;
import com.campus.lostfound.security.RestAuthenticationEntryPoint;
import com.campus.lostfound.service.AnalyticsService;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemReportService;
import com.campus.lostfound.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        JwtAuthenticationFilter.class
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ItemReportService itemReportService;

    @MockitoBean
    private ClaimService claimService;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("Public endpoint: GET /api/reports is accessible without authentication")
    void publicCatalogEndpointShouldBeAccessibleWithoutToken() throws Exception {
        when(itemReportService.getReportsByTypeAndStatus(null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected endpoint: POST /api/reports rejects unauthenticated request with 401")
    void protectedEndpointShouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Bag\",\"description\":\"Blue bag\",\"category\":\"Electronics\",\"location\":\"Library\",\"reportType\":\"LOST\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Protected endpoint: GET /api/analytics rejects unauthenticated request with 401")
    void analyticsEndpointShouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/analytics/unclaimed-items/by-location"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student authorization: Student CANNOT access admin analytics (403 Forbidden)")
    void studentCannotAccessAdminAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/unclaimed-items/by-location"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student authorization: Student CANNOT approve claims (403 Forbidden)")
    void studentCannotApproveClaim() throws Exception {
        mockMvc.perform(patch("/api/claims/1/approve"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student authorization: Student CANNOT reject claims (403 Forbidden)")
    void studentCannotRejectClaim() throws Exception {
        mockMvc.perform(patch("/api/claims/1/reject"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student authorization: Student CANNOT mark items returned (403 Forbidden)")
    void studentCannotMarkItemReturned() throws Exception {
        mockMvc.perform(patch("/api/reports/1/returned"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin authorization: Administrator CAN approve claims (200 OK)")
    void adminCanApproveClaim() throws Exception {
        User claimant = new User("24BCS10001", "Bob", "bob@campus.edu", "hash", Role.STUDENT);
        claimant.setId(2L);
        ItemReport report = new ItemReport("Watch", "Silver", "Accessories", "Cafeteria", ReportType.FOUND, LocalDate.now(), ItemStatus.CLAIMED, claimant);
        report.setId(10L);
        Claim claim = new Claim(report, claimant, "Proof", LocalDate.now(), ClaimStatus.APPROVED);
        claim.setId(1L);

        when(claimService.approveClaim(1L)).thenReturn(claim);

        mockMvc.perform(patch("/api/claims/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimStatus").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin authorization: Administrator CAN access analytics (200 OK)")
    void adminCanAccessAnalytics() throws Exception {
        when(analyticsService.getUnclaimedItemCountsByLocation()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/analytics/unclaimed-items/by-location"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unauthenticated request to GET /api/auth/me returns 401 Unauthorized")
    void unauthenticatedCannotAccessAuthMe() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student authorization: Student CANNOT invoke legacy account linking (403 Forbidden)")
    void studentCannotInvokeLegacyLinking() throws Exception {
        String payload = """
                {
                    "collegeUid": "24BCS10999",
                    "password": "Password12345!"
                }
                """;
        mockMvc.perform(post("/api/users/1/link-legacy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student authorization: Student CANNOT invoke administrative user creation (403 Forbidden)")
    void studentCannotInvokeUserCreation() throws Exception {
        String payload = """
                {
                    "name": "Eve Student",
                    "email": "eve@campus.edu"
                }
                """;
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin authorization: Administrator CAN invoke legacy account linking (200 OK)")
    void adminCanInvokeLegacyLinking() throws Exception {
        com.campus.lostfound.dto.UserResponse response = new com.campus.lostfound.dto.UserResponse(5L, "Dave Miller", "dave.miller@campus.edu");
        when(authService.linkLegacyAccountByAdmin(eq(5L), eq("24BCS10999"), eq("Password12345!"))).thenReturn(response);

        String payload = """
                {
                    "collegeUid": "24BCS10999",
                    "password": "Password12345!"
                }
                """;
        mockMvc.perform(post("/api/users/5/link-legacy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("dave.miller@campus.edu"));
    }
}

