package com.Gemini.AssetVault.Controller;

import com.Gemini.AssetVault.Dto.AssetRequest;
import com.Gemini.AssetVault.Dto.AssetResponse;
import com.Gemini.AssetVault.Exception.AssetNotFoundException;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
import com.Gemini.AssetVault.Service.AssetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetController.class)
class AssetControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AssetService assetService;

    @Test
    void getByIdReturnsAsset() throws Exception {
        when(assetService.getById(1L)).thenReturn(assetResponse());

        mockMvc.perform(get("/api/v1/assets/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetCode").value("LPT-00001"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void createValidatesRequestBody() throws Exception {
        AssetRequest invalid = new AssetRequest(
                "",
                "Apple",
                "M3 Pro",
                AssetType.LAPTOP,
                AssetStatus.AVAILABLE,
                LocalDate.now(),
                BigDecimal.valueOf(149999),
                LocalDate.now().plusYears(3),
                "SER-001",
                "Bengaluru HQ",
                null
        );

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void notFoundExceptionUsesStandardErrorShape() throws Exception {
        when(assetService.getById(99L)).thenThrow(new AssetNotFoundException("Asset ID 99 does not exist"));

        mockMvc.perform(get("/api/v1/assets/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Asset ID 99 does not exist"))
                .andExpect(jsonPath("$.path").value("/api/v1/assets/99"));
    }

    @Test
    void createReturnsCreatedAsset() throws Exception {
        AssetRequest request = new AssetRequest(
                "MacBook Pro 14",
                "Apple",
                "M3 Pro",
                AssetType.LAPTOP,
                null,
                LocalDate.now(),
                BigDecimal.valueOf(149999),
                LocalDate.now().plusYears(3),
                "SER-001",
                "Bengaluru HQ",
                "Onboarding"
        );
        when(assetService.create(any(AssetRequest.class))).thenReturn(assetResponse());

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assetCode").value("LPT-00001"));
    }

    private AssetResponse assetResponse() {
        return new AssetResponse(
                1L,
                "LPT-00001",
                "MacBook Pro 14",
                "Apple",
                "M3 Pro",
                AssetType.LAPTOP,
                AssetStatus.AVAILABLE,
                LocalDate.now(),
                BigDecimal.valueOf(149999),
                LocalDate.now().plusYears(3),
                "SER-001",
                "Bengaluru HQ",
                null,
                null,
                null
        );
    }
}
