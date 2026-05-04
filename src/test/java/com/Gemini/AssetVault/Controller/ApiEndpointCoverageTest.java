package com.Gemini.AssetVault.Controller;

import com.Gemini.AssetVault.Dto.AssetAssignmentResponse;
import com.Gemini.AssetVault.Dto.AssetResponse;
import com.Gemini.AssetVault.Dto.AssetValueResponse;
import com.Gemini.AssetVault.Dto.DashboardSummaryResponse;
import com.Gemini.AssetVault.Dto.DepartmentAssetSummaryResponse;
import com.Gemini.AssetVault.Dto.EmployeeResponse;
import com.Gemini.AssetVault.Dto.LicenseAlertsResponse;
import com.Gemini.AssetVault.Dto.MaintenanceRecordResponse;
import com.Gemini.AssetVault.Dto.MaintenanceSummaryResponse;
import com.Gemini.AssetVault.Dto.SoftwareLicenseResponse;
import com.Gemini.AssetVault.Dto.TypeBreakdownResponse;
import com.Gemini.AssetVault.Exception.AssetAlreadyAssignedException;
import com.Gemini.AssetVault.Exception.AssetNotAvailableException;
import com.Gemini.AssetVault.Exception.AssetNotFoundException;
import com.Gemini.AssetVault.Exception.AssetRetiredException;
import com.Gemini.AssetVault.Exception.AssignmentNotFoundException;
import com.Gemini.AssetVault.Exception.DuplicateEmployeeException;
import com.Gemini.AssetVault.Exception.DuplicateLicenseException;
import com.Gemini.AssetVault.Exception.DuplicateSerialNumberException;
import com.Gemini.AssetVault.Exception.EmployeeHasActiveAssetsException;
import com.Gemini.AssetVault.Exception.EmployeeNotFoundException;
import com.Gemini.AssetVault.Exception.InactiveEmployeeException;
import com.Gemini.AssetVault.Exception.LicenseAlreadyAssignedException;
import com.Gemini.AssetVault.Exception.LicenseExpiredException;
import com.Gemini.AssetVault.Exception.LicenseNotFoundException;
import com.Gemini.AssetVault.Exception.MaintenanceRecordNotFoundException;
import com.Gemini.AssetVault.Exception.NoLicenseSeatsAvailableException;
import com.Gemini.AssetVault.Model.Enum.AssetStatus;
import com.Gemini.AssetVault.Model.Enum.AssetType;
import com.Gemini.AssetVault.Model.Enum.AssignmentStatus;
import com.Gemini.AssetVault.Model.Enum.LicenseType;
import com.Gemini.AssetVault.Model.Enum.MaintenanceStatus;
import com.Gemini.AssetVault.Model.Enum.MaintenanceType;
import com.Gemini.AssetVault.Service.AssetAssignmentService;
import com.Gemini.AssetVault.Service.AssetService;
import com.Gemini.AssetVault.Service.DashboardService;
import com.Gemini.AssetVault.Service.EmployeeService;
import com.Gemini.AssetVault.Service.MaintenanceService;
import com.Gemini.AssetVault.Service.SoftwareLicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AssetController.class,
        EmployeeController.class,
        AssignmentController.class,
        MaintenanceController.class,
        LicenseController.class,
        DashboardController.class
})
class ApiEndpointCoverageTest {
    private static final String ASSET_BODY = """
            {
              "name": "MacBook Pro 14",
              "brand": "Apple",
              "model": "M3 Pro",
              "type": "LAPTOP",
              "status": "AVAILABLE",
              "purchaseDate": "2026-04-01",
              "purchaseCost": 149999.00,
              "warrantyExpiryDate": "2029-04-01",
              "serialNumber": "SER-001",
              "location": "Bengaluru HQ",
              "notes": "Onboarding"
            }
            """;
    private static final String EMPLOYEE_BODY = """
            {
              "name": "Aarav Mehta",
              "email": "aarav@example.com",
              "department": "Engineering",
              "designation": "Engineer",
              "employeeCode": "EMP-00001"
            }
            """;
    private static final String ASSIGNMENT_BODY = """
            {
              "assetId": 1,
              "employeeId": 1,
              "assignedDate": "2026-04-21",
              "assignedBy": "IT Admin",
              "remarks": "Issued during onboarding"
            }
            """;
    private static final String MAINTENANCE_BODY = """
            {
              "assetId": 1,
              "maintenanceType": "REPAIR",
              "description": "Keyboard replacement",
              "maintenanceCost": 3500.00,
              "vendor": "Apple Service",
              "scheduledDate": "2026-05-10",
              "completedDate": null,
              "status": "SCHEDULED"
            }
            """;
    private static final String LICENSE_BODY = """
            {
              "softwareName": "IntelliJ IDEA",
              "licenseKey": "LIC-IDEA-2026-001",
              "licenseType": "FLOATING",
              "vendor": "JetBrains",
              "totalSeats": 25,
              "usedSeats": 0,
              "purchaseDate": "2026-04-01",
              "purchaseCost": 250000.00,
              "expiryDate": "2027-04-01",
              "active": true
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssetService assetService;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private AssetAssignmentService assignmentService;

    @MockitoBean
    private MaintenanceService maintenanceService;

    @MockitoBean
    private SoftwareLicenseService softwareLicenseService;

    @MockitoBean
    private DashboardService dashboardService;

    @BeforeEach
    void stubServices() {
        AssetResponse asset = assetResponse();
        EmployeeResponse employee = employeeResponse();
        AssetAssignmentResponse assignment = assignmentResponse();
        MaintenanceRecordResponse maintenance = maintenanceResponse();
        SoftwareLicenseResponse license = licenseResponse();

        lenient().when(assetService.create(any())).thenReturn(asset);
        lenient().when(assetService.getAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(asset)));
        lenient().when(assetService.getById(anyLong())).thenReturn(asset);
        lenient().when(assetService.getByCode(any())).thenReturn(asset);
        lenient().when(assetService.getByType(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(asset)));
        lenient().when(assetService.getByStatus(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(asset)));
        lenient().when(assetService.getAvailable(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(asset)));
        lenient().when(assetService.getByDepartment(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(asset)));
        lenient().when(assetService.getWarrantyExpiring(anyInt(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(asset)));
        lenient().when(assetService.getWarrantyExpired(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(asset)));
        lenient().when(assetService.search(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(asset)));
        lenient().when(assetService.getLowValue(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(asset)));
        lenient().when(assetService.update(anyLong(), any())).thenReturn(asset);
        lenient().when(assetService.updateStatus(anyLong(), any())).thenReturn(asset);
        lenient().when(assetService.retire(anyLong())).thenReturn(asset);
        lenient().when(assetService.markLost(anyLong())).thenReturn(asset);

        lenient().when(employeeService.create(any())).thenReturn(employee);
        lenient().when(employeeService.getAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(employee)));
        lenient().when(employeeService.getById(anyLong())).thenReturn(employee);
        lenient().when(employeeService.searchByName(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(employee)));
        lenient().when(employeeService.getByDepartment(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(employee)));
        lenient().when(employeeService.getAssignedAssets(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(asset)));
        lenient().when(employeeService.getAssignedLicenses(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(license)));
        lenient().when(employeeService.getAssignmentHistory(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(assignment)));
        lenient().when(employeeService.update(anyLong(), any())).thenReturn(employee);
        lenient().when(employeeService.deactivate(anyLong())).thenReturn(employee);

        lenient().when(assignmentService.assign(any())).thenReturn(assignment);
        lenient().when(assignmentService.getAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(assignment)));
        lenient().when(assignmentService.getById(anyLong())).thenReturn(assignment);
        lenient().when(assignmentService.getActive(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(assignment)));
        lenient().when(assignmentService.getByAsset(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(assignment)));
        lenient().when(assignmentService.getByEmployee(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(assignment)));
        lenient().when(assignmentService.getByDateRange(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(assignment)));
        lenient().when(assignmentService.returnAsset(anyLong())).thenReturn(assignment);
        lenient().when(assignmentService.transfer(anyLong(), anyLong())).thenReturn(assignment);

        lenient().when(maintenanceService.create(any())).thenReturn(maintenance);
        lenient().when(maintenanceService.getAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(maintenance)));
        lenient().when(maintenanceService.getById(anyInt())).thenReturn(maintenance);
        lenient().when(maintenanceService.getByAsset(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(maintenance)));
        lenient().when(maintenanceService.getByStatus(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(maintenance)));
        lenient().when(maintenanceService.getByType(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(maintenance)));
        lenient().when(maintenanceService.getScheduled(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(maintenance)));
        lenient().when(maintenanceService.getByDateRange(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(maintenance)));
        lenient().when(maintenanceService.update(anyInt(), any())).thenReturn(maintenance);
        lenient().when(maintenanceService.complete(anyInt())).thenReturn(maintenance);
        lenient().when(maintenanceService.cancel(anyInt())).thenReturn(maintenance);

        lenient().when(softwareLicenseService.create(any())).thenReturn(license);
        lenient().when(softwareLicenseService.getAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(license)));
        lenient().when(softwareLicenseService.getById(anyLong())).thenReturn(license);
        lenient().when(softwareLicenseService.getExpiringSoon(anyInt(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(license)));
        lenient().when(softwareLicenseService.getExpired(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(license)));
        lenient().when(softwareLicenseService.getLowSeats(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(license)));
        lenient().when(softwareLicenseService.search(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(license)));
        lenient().when(softwareLicenseService.update(anyLong(), any())).thenReturn(license);
        lenient().when(softwareLicenseService.assign(anyLong(), anyLong())).thenReturn(license);
        lenient().when(softwareLicenseService.revoke(anyLong(), anyLong())).thenReturn(license);
        lenient().when(softwareLicenseService.deactivate(anyLong())).thenReturn(license);

        lenient().when(dashboardService.getSummary()).thenReturn(new DashboardSummaryResponse(10, 4, 5, 1, 0));
        lenient().when(dashboardService.getTypeBreakdown()).thenReturn(List.of(new TypeBreakdownResponse(AssetType.LAPTOP, 4)));
        lenient().when(dashboardService.getDepartmentAssets()).thenReturn(List.of(new DepartmentAssetSummaryResponse("Engineering", 4, BigDecimal.valueOf(4000))));
        lenient().when(dashboardService.getWarrantyAlerts()).thenReturn(List.of(asset));
        lenient().when(dashboardService.getLicenseAlerts()).thenReturn(new LicenseAlertsResponse(List.of(license), List.of(license)));
        lenient().when(dashboardService.getMaintenanceSummary()).thenReturn(new MaintenanceSummaryResponse(2, 1, 7));
        lenient().when(dashboardService.getAssetValue()).thenReturn(new AssetValueResponse(BigDecimal.valueOf(4000)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("successEndpoints")
    void everyEndpointHasSuccessPath(EndpointCase endpoint) throws Exception {
        MockHttpServletRequestBuilder request = request(endpoint.method(), endpoint.path());
        if (endpoint.body() != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(endpoint.body());
        }

        mockMvc.perform(request)
                .andExpect(status().is(endpoint.expectedStatus()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("exceptionCases")
    void customExceptionsUseExpectedErrorShape(ExceptionCase exceptionCase) throws Exception {
        when(assetService.getById(eq(404L))).thenThrow(exceptionCase.exception());

        mockMvc.perform(get("/api/v1/assets/{id}", 404L))
                .andExpect(status().is(exceptionCase.status()))
                .andExpect(jsonPath("$.status").value(exceptionCase.status()))
                .andExpect(jsonPath("$.error").value(exceptionCase.error()))
                .andExpect(jsonPath("$.path").value("/api/v1/assets/404"));
    }

    static Stream<EndpointCase> successEndpoints() {
        return Stream.of(
                endpoint("POST", "/api/v1/assets", ASSET_BODY, 201),
                endpoint("GET", "/api/v1/assets", null, 200),
                endpoint("GET", "/api/v1/assets/1", null, 200),
                endpoint("GET", "/api/v1/assets/code/LPT-00001", null, 200),
                endpoint("GET", "/api/v1/assets/type/LAPTOP", null, 200),
                endpoint("GET", "/api/v1/assets/status/AVAILABLE", null, 200),
                endpoint("GET", "/api/v1/assets/available", null, 200),
                endpoint("GET", "/api/v1/assets/department?name=Engineering", null, 200),
                endpoint("GET", "/api/v1/assets/warranty-expiring?days=30", null, 200),
                endpoint("GET", "/api/v1/assets/warranty-expired", null, 200),
                endpoint("GET", "/api/v1/assets/search?keyword=mac", null, 200),
                endpoint("GET", "/api/v1/assets/low-value?maxCost=1000", null, 200),
                endpoint("PUT", "/api/v1/assets/1", ASSET_BODY, 200),
                endpoint("PATCH", "/api/v1/assets/1/status?status=LOST", null, 200),
                endpoint("PATCH", "/api/v1/assets/1/retire", null, 200),
                endpoint("PATCH", "/api/v1/assets/1/mark-lost", null, 200),
                endpoint("DELETE", "/api/v1/assets/1", null, 204),

                endpoint("POST", "/api/v1/employees", EMPLOYEE_BODY, 201),
                endpoint("GET", "/api/v1/employees", null, 200),
                endpoint("GET", "/api/v1/employees/1", null, 200),
                endpoint("GET", "/api/v1/employees/search?name=Aarav", null, 200),
                endpoint("GET", "/api/v1/employees/department?name=Engineering", null, 200),
                endpoint("GET", "/api/v1/employees/1/assets", null, 200),
                endpoint("GET", "/api/v1/employees/1/licenses", null, 200),
                endpoint("GET", "/api/v1/employees/1/assignment-history", null, 200),
                endpoint("PUT", "/api/v1/employees/1", EMPLOYEE_BODY, 200),
                endpoint("PATCH", "/api/v1/employees/1/deactivate", null, 200),
                endpoint("DELETE", "/api/v1/employees/1", null, 204),

                endpoint("POST", "/api/v1/assignments", ASSIGNMENT_BODY, 201),
                endpoint("GET", "/api/v1/assignments", null, 200),
                endpoint("GET", "/api/v1/assignments/1", null, 200),
                endpoint("GET", "/api/v1/assignments/active", null, 200),
                endpoint("GET", "/api/v1/assignments/asset/1", null, 200),
                endpoint("GET", "/api/v1/assignments/employee/1", null, 200),
                endpoint("GET", "/api/v1/assignments/date-range?from=2026-04-01&to=2026-04-30", null, 200),
                endpoint("PATCH", "/api/v1/assignments/1/return", null, 200),
                endpoint("PATCH", "/api/v1/assignments/1/transfer?toEmployeeId=2", null, 200),
                endpoint("DELETE", "/api/v1/assignments/1", null, 204),

                endpoint("POST", "/api/v1/maintenance", MAINTENANCE_BODY, 201),
                endpoint("GET", "/api/v1/maintenance", null, 200),
                endpoint("GET", "/api/v1/maintenance/1", null, 200),
                endpoint("GET", "/api/v1/maintenance/asset/1", null, 200),
                endpoint("GET", "/api/v1/maintenance/status/SCHEDULED", null, 200),
                endpoint("GET", "/api/v1/maintenance/type/REPAIR", null, 200),
                endpoint("GET", "/api/v1/maintenance/scheduled", null, 200),
                endpoint("GET", "/api/v1/maintenance/date-range?from=2026-05-01&to=2026-05-30", null, 200),
                endpoint("PUT", "/api/v1/maintenance/1", MAINTENANCE_BODY, 200),
                endpoint("PATCH", "/api/v1/maintenance/1/complete", null, 200),
                endpoint("PATCH", "/api/v1/maintenance/1/cancel", null, 200),
                endpoint("DELETE", "/api/v1/maintenance/1", null, 204),

                endpoint("POST", "/api/v1/licenses", LICENSE_BODY, 201),
                endpoint("GET", "/api/v1/licenses", null, 200),
                endpoint("GET", "/api/v1/licenses/1", null, 200),
                endpoint("GET", "/api/v1/licenses/expiring-soon?days=30", null, 200),
                endpoint("GET", "/api/v1/licenses/expired", null, 200),
                endpoint("GET", "/api/v1/licenses/low-seats", null, 200),
                endpoint("GET", "/api/v1/licenses/search?name=IntelliJ", null, 200),
                endpoint("PUT", "/api/v1/licenses/1", LICENSE_BODY, 200),
                endpoint("PATCH", "/api/v1/licenses/1/assign?employeeId=1", null, 200),
                endpoint("PATCH", "/api/v1/licenses/1/revoke?employeeId=1", null, 200),
                endpoint("PATCH", "/api/v1/licenses/1/deactivate", null, 200),
                endpoint("DELETE", "/api/v1/licenses/1", null, 204),

                endpoint("GET", "/api/v1/dashboard/summary", null, 200),
                endpoint("GET", "/api/v1/dashboard/type-breakdown", null, 200),
                endpoint("GET", "/api/v1/dashboard/department-assets", null, 200),
                endpoint("GET", "/api/v1/dashboard/warranty-alerts", null, 200),
                endpoint("GET", "/api/v1/dashboard/license-alerts", null, 200),
                endpoint("GET", "/api/v1/dashboard/maintenance-summary", null, 200),
                endpoint("GET", "/api/v1/dashboard/asset-value", null, 200)
        );
    }

    static Stream<ExceptionCase> exceptionCases() {
        return Stream.of(
                exception(new AssetNotFoundException("missing"), 404, "Not Found"),
                exception(new EmployeeNotFoundException("missing"), 404, "Not Found"),
                exception(new AssignmentNotFoundException("missing"), 404, "Not Found"),
                exception(new MaintenanceRecordNotFoundException("missing"), 404, "Not Found"),
                exception(new LicenseNotFoundException("missing"), 404, "Not Found"),
                exception(new AssetNotAvailableException("not available"), 409, "Conflict"),
                exception(new AssetRetiredException("retired"), 409, "Conflict"),
                exception(new AssetAlreadyAssignedException("already assigned"), 409, "Conflict"),
                exception(new InactiveEmployeeException("inactive"), 409, "Conflict"),
                exception(new NoLicenseSeatsAvailableException("no seats"), 409, "Conflict"),
                exception(new LicenseAlreadyAssignedException("already licensed"), 409, "Conflict"),
                exception(new EmployeeHasActiveAssetsException("active assets"), 409, "Conflict"),
                exception(new DuplicateSerialNumberException("duplicate serial"), 409, "Conflict"),
                exception(new DuplicateEmployeeException("duplicate employee"), 409, "Conflict"),
                exception(new DuplicateLicenseException("duplicate license"), 409, "Conflict"),
                exception(new DataIntegrityViolationException("constraint"), 409, "Conflict"),
                exception(new LicenseExpiredException("expired"), 422, "Unprocessable Content"),
                exception(new IllegalArgumentException("bad request"), 400, "Bad Request")
        );
    }

    private static EndpointCase endpoint(String method, String path, String body, int expectedStatus) {
        return new EndpointCase(method, path, body, expectedStatus);
    }

    private static ExceptionCase exception(RuntimeException exception, int status, String error) {
        return new ExceptionCase(exception, status, error);
    }

    private MockHttpServletRequestBuilder request(String method, String path) {
        return switch (method) {
            case "GET" -> get(path);
            case "POST" -> post(path);
            case "PUT" -> put(path);
            case "PATCH" -> patch(path);
            case "DELETE" -> delete(path);
            default -> throw new IllegalArgumentException("Unsupported method " + method);
        };
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
                LocalDate.of(2026, 4, 1),
                BigDecimal.valueOf(149999),
                LocalDate.of(2029, 4, 1),
                "SER-001",
                "Bengaluru HQ",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private EmployeeResponse employeeResponse() {
        return new EmployeeResponse(
                1L,
                "Aarav Mehta",
                "aarav@example.com",
                "Engineering",
                "Engineer",
                "EMP-00001",
                true,
                LocalDateTime.now()
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
                LocalDate.of(2026, 4, 21),
                null,
                AssignmentStatus.ACTIVE,
                "IT Admin",
                "Issued during onboarding",
                LocalDateTime.now()
        );
    }

    private MaintenanceRecordResponse maintenanceResponse() {
        return new MaintenanceRecordResponse(
                1,
                1L,
                "LPT-00001",
                "MacBook Pro 14",
                MaintenanceType.REPAIR,
                "Keyboard replacement",
                BigDecimal.valueOf(3500),
                "Apple Service",
                LocalDate.of(2026, 5, 10),
                null,
                MaintenanceStatus.SCHEDULED,
                LocalDateTime.now()
        );
    }

    private SoftwareLicenseResponse licenseResponse() {
        return new SoftwareLicenseResponse(
                1L,
                "IntelliJ IDEA",
                "LIC-IDEA-2026-001",
                LicenseType.FLOATING,
                "JetBrains",
                25,
                0,
                25,
                LocalDate.of(2026, 4, 1),
                BigDecimal.valueOf(250000),
                LocalDate.of(2027, 4, 1),
                true,
                List.of(employeeResponse()),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    record EndpointCase(String method, String path, String body, int expectedStatus) {
        @Override
        public String toString() {
            return method + " " + path;
        }
    }

    record ExceptionCase(RuntimeException exception, int status, String error) {
        @Override
        public String toString() {
            return exception.getClass().getSimpleName();
        }
    }
}
