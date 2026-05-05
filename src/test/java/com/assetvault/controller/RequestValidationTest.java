package com.assetvault.controller;

import com.assetvault.service.AssetService;
import com.assetvault.service.AssetAssignmentService;
import com.assetvault.service.SoftwareLicenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AssetController.class,
        AssignmentController.class,
        LicenseController.class
})
class RequestValidationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssetService assetService;

    @MockitoBean
    private AssetAssignmentService assignmentService;

    @MockitoBean
    private SoftwareLicenseService softwareLicenseService;

    @Test
    void queryParameterConstraintViolationsUseStandardErrorShape() throws Exception {
        mockMvc.perform(get("/api/v1/assets/warranty-expiring").param("days", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request parameter validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/assets/warranty-expiring"))
                .andExpect(jsonPath("$.fieldErrors.*", hasItem("must be greater than or equal to 1")));

        verifyNoInteractions(assetService);
    }

    @Test
    void typeMismatchesUseStandardErrorShapeBeforeCallingService() throws Exception {
        mockMvc.perform(patch("/api/v1/assets/1/status").param("status", "BROKEN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("Failed to convert")))
                .andExpect(jsonPath("$.path").value("/api/v1/assets/1/status"));

        verifyNoInteractions(assetService);
    }

    @Test
    void missingRequestParametersUseStandardErrorShapeBeforeCallingService() throws Exception {
        mockMvc.perform(patch("/api/v1/licenses/1/assign"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request parameter validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/licenses/1/assign"))
                .andExpect(jsonPath("$.fieldErrors.employeeId").value("must be provided"));

        verifyNoInteractions(softwareLicenseService);
    }

    @Test
    void invalidPathVariablesUseStandardErrorShapeBeforeCallingService() throws Exception {
        mockMvc.perform(patch("/api/v1/assignments/0/return"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request parameter validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/assignments/0/return"))
                .andExpect(jsonPath("$.fieldErrors.*", hasItem("must be greater than 0")));

        verifyNoInteractions(assignmentService);
    }

    @Test
    void malformedRequestBodiesUseStandardErrorShapeBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed request body"))
                .andExpect(jsonPath("$.path").value("/api/v1/assets"));

        verifyNoInteractions(assetService);
    }

    @Test
    void requestBodyValidationReportsEveryInvalidLicenseField() throws Exception {
        String body = """
                {
                  "softwareName": " ",
                  "licenseKey": "",
                  "vendor": "",
                  "totalSeats": 0,
                  "usedSeats": -1,
                  "purchaseDate": "%s",
                  "purchaseCost": -10.00
                }
                """.formatted(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/v1/licenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/licenses"))
                .andExpect(jsonPath("$.fieldErrors.softwareName").value("must not be blank"))
                .andExpect(jsonPath("$.fieldErrors.licenseKey").value("must not be blank"))
                .andExpect(jsonPath("$.fieldErrors.licenseType").value("must not be null"))
                .andExpect(jsonPath("$.fieldErrors.vendor").value("must not be blank"))
                .andExpect(jsonPath("$.fieldErrors.totalSeats").value("must be greater than or equal to 1"))
                .andExpect(jsonPath("$.fieldErrors.usedSeats").value("must be greater than or equal to 0"))
                .andExpect(jsonPath("$.fieldErrors.purchaseDate").value("must be a date in the past or in the present"))
                .andExpect(jsonPath("$.fieldErrors.purchaseCost").value("must be greater than or equal to 0.00"));

        verifyNoInteractions(softwareLicenseService);
    }
}
