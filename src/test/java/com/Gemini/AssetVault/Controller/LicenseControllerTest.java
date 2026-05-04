package com.Gemini.AssetVault.Controller;

import com.Gemini.AssetVault.Exception.LicenseExpiredException;
import com.Gemini.AssetVault.Service.SoftwareLicenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LicenseController.class)
class LicenseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SoftwareLicenseService softwareLicenseService;

    @Test
    void expiredLicenseAssignmentUsesUnprocessableContentError() throws Exception {
        when(softwareLicenseService.assign(1L, 7L))
                .thenThrow(new LicenseExpiredException("Cannot assign an expired software license"));

        mockMvc.perform(patch("/api/v1/licenses/{id}/assign", 1L).param("employeeId", "7"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.message").value("Cannot assign an expired software license"));
    }
}
