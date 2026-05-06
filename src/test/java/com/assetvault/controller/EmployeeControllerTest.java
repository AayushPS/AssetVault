package com.assetvault.controller;

import com.assetvault.dto.AssetAssignmentResponse;
import com.assetvault.dto.AssetResponse;
import com.assetvault.dto.EmployeeRequest;
import com.assetvault.dto.EmployeeResponse;
import com.assetvault.dto.SoftwareLicenseResponse;
import com.assetvault.exception.EmployeeHasActiveAssetsException;
import com.assetvault.exception.EmployeeNotFoundException;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.enums.LicenseType;
import com.assetvault.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void createReturns201WithGeneratedCode() throws Exception {
        // TC-001: POST /employees — Success
        when(employeeService.create(any(EmployeeRequest.class))).thenReturn(employeeResponse());

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Aarav Mehta"))
                .andExpect(jsonPath("$.employeeCode").value("EMP-00001"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createRejectsInvalidRequestBody() throws Exception {
        // TC-002: POST /employees — Validation failure
        EmployeeRequest invalidRequest = new EmployeeRequest(
                " ",
                "not-an-email",
                "Engineering",
                "Senior Engineer",
                null
        );

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").value("must not be blank"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        verifyNoInteractions(employeeService);
    }

    @Test
    void getAllReturnsPaginatedEmployees() throws Exception {
        // TC-003: GET /employees — Paginated list
        when(employeeService.getAll(any(Pageable.class))).thenReturn(pageOf(employeeResponse(), 5));

        mockMvc.perform(get("/api/v1/employees").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].employeeCode").value("EMP-00001"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    void getByIdReturns404WhenEmployeeDoesNotExist() throws Exception {
        // TC-004: GET /employees/{id} — Not found
        when(employeeService.getById(999L)).thenThrow(new EmployeeNotFoundException("Employee ID 999 does not exist"));

        mockMvc.perform(get("/api/v1/employees/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Employee ID 999 does not exist"))
                .andExpect(jsonPath("$.path").value("/api/v1/employees/999"));
    }

    @Test
    void deactivateReturns409WhenEmployeeHasActiveAssets() throws Exception {
        // TC-005: PATCH /employees/{id}/deactivate — Employee with active assets
        when(employeeService.deactivate(1L))
                .thenThrow(new EmployeeHasActiveAssetsException("Employee has active assets and cannot be deactivated"));

        mockMvc.perform(patch("/api/v1/employees/{id}/deactivate", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Employee has active assets and cannot be deactivated"))
                .andExpect(jsonPath("$.path").value("/api/v1/employees/1/deactivate"));
    }

    @Test
    void assignedAssetsReturnsEmployeeAssetPage() throws Exception {
        // TC-006: GET /employees/{id}/assets — Success
        when(employeeService.getAssignedAssets(eq(1L), any(Pageable.class))).thenReturn(pageOf(assetResponse(), 20));

        mockMvc.perform(get("/api/v1/employees/{id}/assets", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].assetCode").value("LPT-00001"))
                .andExpect(jsonPath("$.content[0].status").value("ASSIGNED"));
    }

    @Test
    void assignedLicensesReturnsEmployeeLicensePage() throws Exception {
        // TC-007: GET /employees/{id}/licenses — Success
        when(employeeService.getAssignedLicenses(eq(1L), any(Pageable.class))).thenReturn(pageOf(licenseResponse(), 20));

        mockMvc.perform(get("/api/v1/employees/{id}/licenses", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].softwareName").value("IntelliJ IDEA"))
                .andExpect(jsonPath("$.content[0].remainingSeats").value(4));
    }

    @Test
    void deleteReturns204NoContent() throws Exception {
        // TC-008: DELETE /employees/{id} — Success
        mockMvc.perform(delete("/api/v1/employees/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(employeeService).delete(1L);
    }

    @Test
    void searchRejectsBlankNameParameter() throws Exception {
        // TC-075: GET /employees/search?name= — Blank name
        mockMvc.perform(get("/api/v1/employees/search").param("name", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request parameter validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/employees/search"))
                .andExpect(jsonPath("$.fieldErrors.*", hasItem("must not be blank")));

        verifyNoInteractions(employeeService);
    }

    @Test
    void byDepartmentReturnsEmployeesForDepartment() throws Exception {
        // TC-076: GET /employees/department?name= — Valid
        when(employeeService.getByDepartment(eq("Engineering"), any(Pageable.class))).thenReturn(pageOf(employeeResponse(), 20));

        mockMvc.perform(get("/api/v1/employees/department").param("name", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department").value("Engineering"))
                .andExpect(jsonPath("$.content[0].employeeCode").value("EMP-00001"));
    }

    @Test
    void assignmentHistoryReturnsEmployeeAssignments() throws Exception {
        // TC-077: GET /employees/{id}/assignment-history — Valid
        when(employeeService.getAssignmentHistory(eq(1L), any(Pageable.class))).thenReturn(pageOf(assignmentResponse(), 20));

        mockMvc.perform(get("/api/v1/employees/{id}/assignment-history", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].assetCode").value("LPT-00001"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    void updateReturnsUpdatedEmployee() throws Exception {
        // TC-078: PUT /employees/{id} — Valid update
        EmployeeRequest request = new EmployeeRequest(
                "Aarav Mehta",
                "aarav.mehta@example.com",
                "Engineering",
                "Staff Engineer",
                "EMP-00001"
        );
        when(employeeService.update(eq(1L), any(EmployeeRequest.class))).thenReturn(updatedEmployeeResponse());

        mockMvc.perform(put("/api/v1/employees/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designation").value("Staff Engineer"))
                .andExpect(jsonPath("$.employeeCode").value("EMP-00001"));
    }

    private Page<EmployeeResponse> pageOf(EmployeeResponse response, int size) {
        return new PageImpl<>(List.of(response), PageRequest.of(0, size), 1);
    }

    private Page<AssetResponse> pageOf(AssetResponse response, int size) {
        return new PageImpl<>(List.of(response), PageRequest.of(0, size), 1);
    }

    private Page<SoftwareLicenseResponse> pageOf(SoftwareLicenseResponse response, int size) {
        return new PageImpl<>(List.of(response), PageRequest.of(0, size), 1);
    }

    private Page<AssetAssignmentResponse> pageOf(AssetAssignmentResponse response, int size) {
        return new PageImpl<>(List.of(response), PageRequest.of(0, size), 1);
    }

    private EmployeeRequest employeeRequest() {
        return new EmployeeRequest(
                "Aarav Mehta",
                "aarav.mehta@example.com",
                "Engineering",
                "Senior Engineer",
                null
        );
    }

    private EmployeeResponse employeeResponse() {
        return new EmployeeResponse(
                1L,
                "Aarav Mehta",
                "aarav.mehta@example.com",
                "Engineering",
                "Senior Engineer",
                "EMP-00001",
                true,
                LocalDateTime.of(2026, 5, 1, 9, 30)
        );
    }

    private EmployeeResponse updatedEmployeeResponse() {
        return new EmployeeResponse(
                1L,
                "Aarav Mehta",
                "aarav.mehta@example.com",
                "Engineering",
                "Staff Engineer",
                "EMP-00001",
                true,
                LocalDateTime.of(2026, 5, 1, 9, 30)
        );
    }

    private AssetResponse assetResponse() {
        return new AssetResponse(
                1L,
                "LPT-00001",
                "MacBook Pro 14",
                "Apple",
                "M3 Pro",
                AssetType.LAPTOP,
                AssetStatus.ASSIGNED,
                LocalDate.of(2026, 4, 1),
                BigDecimal.valueOf(149999),
                LocalDate.of(2029, 4, 1),
                "SER-001",
                "Bengaluru HQ",
                "Issued during onboarding",
                LocalDateTime.of(2026, 5, 1, 9, 30),
                LocalDateTime.of(2026, 5, 2, 10, 0)
        );
    }

    private SoftwareLicenseResponse licenseResponse() {
        return new SoftwareLicenseResponse(
                1L,
                "IntelliJ IDEA",
                "LIC-IDEA-001",
                LicenseType.INDIVIDUAL,
                "JetBrains",
                5,
                1,
                4,
                LocalDate.of(2026, 4, 1),
                BigDecimal.valueOf(25000),
                LocalDate.of(2027, 4, 1),
                true,
                List.of(employeeResponse()),
                LocalDateTime.of(2026, 5, 1, 9, 30),
                LocalDateTime.of(2026, 5, 2, 10, 0)
        );
    }

    private AssetAssignmentResponse assignmentResponse() {
        return new AssetAssignmentResponse(
                1L,
                1L,
                "LPT-00001",
                "MacBook Pro 14",
                1L,
                "Aarav Mehta",
                LocalDate.of(2026, 5, 1),
                null,
                AssignmentStatus.ACTIVE,
                "IT Admin",
                "Issued during onboarding",
                LocalDateTime.of(2026, 5, 1, 9, 30)
        );
    }
}