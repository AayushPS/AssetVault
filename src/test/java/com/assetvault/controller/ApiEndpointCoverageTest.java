package com.assetvault.controller;

import com.assetvault.dto.AssetAssignmentResponse;
import com.assetvault.dto.AssetResponse;
import com.assetvault.dto.AssetValueResponse;
import com.assetvault.dto.DashboardSummaryResponse;
import com.assetvault.dto.DepartmentAssetSummaryResponse;
import com.assetvault.dto.EmployeeResponse;
import com.assetvault.dto.LicenseAlertsResponse;
import com.assetvault.dto.MaintenanceRecordResponse;
import com.assetvault.dto.MaintenanceSummaryResponse;
import com.assetvault.dto.SoftwareLicenseResponse;
import com.assetvault.dto.TypeBreakdownResponse;
import com.assetvault.exception.AssetAlreadyAssignedException;
import com.assetvault.exception.AssetNotAvailableException;
import com.assetvault.exception.AssetNotFoundException;
import com.assetvault.exception.AssetRetiredException;
import com.assetvault.exception.AssignmentNotFoundException;
import com.assetvault.exception.DuplicateEmployeeException;
import com.assetvault.exception.DuplicateLicenseException;
import com.assetvault.exception.DuplicateSerialNumberException;
import com.assetvault.exception.EmployeeHasActiveAssetsException;
import com.assetvault.exception.EmployeeNotFoundException;
import com.assetvault.exception.InactiveEmployeeException;
import com.assetvault.exception.LicenseAlreadyAssignedException;
import com.assetvault.exception.LicenseExpiredException;
import com.assetvault.exception.LicenseNotFoundException;
import com.assetvault.exception.MaintenanceStateException;
import com.assetvault.exception.MaintenanceRecordNotFoundException;
import com.assetvault.exception.NoLicenseSeatsAvailableException;
import com.assetvault.model.enums.AssetStatus;
import com.assetvault.model.enums.AssetType;
import com.assetvault.model.enums.AssignmentStatus;
import com.assetvault.model.enums.LicenseType;
import com.assetvault.model.enums.MaintenanceStatus;
import com.assetvault.model.enums.MaintenanceType;
import com.assetvault.service.AssetAssignmentService;
import com.assetvault.service.AssetService;
import com.assetvault.service.DashboardService;
import com.assetvault.service.EmployeeService;
import com.assetvault.service.MaintenanceService;
import com.assetvault.service.SoftwareAssignmentService;
import com.assetvault.service.SoftwareLicenseService;
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
import static org.mockito.Mockito.doThrow;
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
    private AssetAssignmentService assignmentService;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private MaintenanceService maintenanceService;

    @MockitoBean
    private SoftwareLicenseService softwareLicenseService;

    @MockitoBean
    private SoftwareAssignmentService softwareAssignmentService;

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
        lenient().when(maintenanceService.start(anyInt())).thenReturn(maintenance);
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
        lenient().when(softwareAssignmentService.assign(anyLong(), anyLong())).thenReturn(license);
        lenient().when(softwareAssignmentService.revoke(anyLong(), anyLong())).thenReturn(license);
        lenient().when(softwareLicenseService.deactivate(anyLong())).thenReturn(license);

        lenient().when(dashboardService.getSummary()).thenReturn(new DashboardSummaryResponse(10, 4, 5, 1, 0));
        lenient().when(dashboardService.getTypeBreakdown(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(new TypeBreakdownResponse(AssetType.LAPTOP, 4))));
        lenient().when(dashboardService.getDepartmentAssets(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(new DepartmentAssetSummaryResponse("Engineering", 4, BigDecimal.valueOf(4000)))));
        lenient().when(dashboardService.getWarrantyAlerts(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(asset)));
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("successEndpoints")
    void everyEndpointHasExceptionPath(EndpointCase endpoint) throws Exception {
        RuntimeException exception = new IllegalArgumentException("Endpoint failure");
        stubEndpointFailure(endpoint, exception);

        MockHttpServletRequestBuilder request = request(endpoint.method(), endpoint.path());
        if (endpoint.body() != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(endpoint.body());
        }

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Endpoint failure"))
                .andExpect(jsonPath("$.path").value(endpoint.pathWithoutQuery()));
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
                endpoint("PATCH", "/api/v1/maintenance/1/start", null, 200),
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
                exception(new MaintenanceStateException("invalid maintenance state"), 409, "Conflict"),
                exception(new DataIntegrityViolationException("constraint"), 409, "Conflict"),
                exception(new LicenseExpiredException("expired"), 422, "Unprocessable Entity"),
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

    private void stubEndpointFailure(EndpointCase endpoint, RuntimeException exception) {
        switch (endpoint.key()) {
            case "POST /api/v1/assets" -> when(assetService.create(any())).thenThrow(exception);
            case "GET /api/v1/assets" -> when(assetService.getAll(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/assets/1" -> when(assetService.getById(1L)).thenThrow(exception);
            case "GET /api/v1/assets/code/LPT-00001" -> when(assetService.getByCode("LPT-00001")).thenThrow(exception);
            case "GET /api/v1/assets/type/LAPTOP" ->
                    when(assetService.getByType(eq(AssetType.LAPTOP), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/assets/status/AVAILABLE" ->
                    when(assetService.getByStatus(eq(AssetStatus.AVAILABLE), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/assets/available" ->
                    when(assetService.getAvailable(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/assets/department" ->
                    when(assetService.getByDepartment(eq("Engineering"), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/assets/warranty-expiring" ->
                    when(assetService.getWarrantyExpiring(eq(30), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/assets/warranty-expired" ->
                    when(assetService.getWarrantyExpired(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/assets/search" ->
                    when(assetService.search(eq("mac"), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/assets/low-value" ->
                    when(assetService.getLowValue(eq(BigDecimal.valueOf(1000)), endpointPageable())).thenThrow(exception);
            case "PUT /api/v1/assets/1" -> when(assetService.update(eq(1L), any())).thenThrow(exception);
            case "PATCH /api/v1/assets/1/status" ->
                    when(assetService.updateStatus(1L, AssetStatus.LOST)).thenThrow(exception);
            case "PATCH /api/v1/assets/1/retire" -> when(assetService.retire(1L)).thenThrow(exception);
            case "PATCH /api/v1/assets/1/mark-lost" -> when(assetService.markLost(1L)).thenThrow(exception);
            case "DELETE /api/v1/assets/1" -> doThrow(exception).when(assetService).delete(1L);

            case "POST /api/v1/employees" -> when(employeeService.create(any())).thenThrow(exception);
            case "GET /api/v1/employees" -> when(employeeService.getAll(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/employees/1" -> when(employeeService.getById(1L)).thenThrow(exception);
            case "GET /api/v1/employees/search" ->
                    when(employeeService.searchByName(eq("Aarav"), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/employees/department" ->
                    when(employeeService.getByDepartment(eq("Engineering"), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/employees/1/assets" ->
                    when(employeeService.getAssignedAssets(eq(1L), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/employees/1/licenses" ->
                    when(employeeService.getAssignedLicenses(eq(1L), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/employees/1/assignment-history" ->
                    when(employeeService.getAssignmentHistory(eq(1L), endpointPageable())).thenThrow(exception);
            case "PUT /api/v1/employees/1" -> when(employeeService.update(eq(1L), any())).thenThrow(exception);
            case "PATCH /api/v1/employees/1/deactivate" -> when(employeeService.deactivate(1L)).thenThrow(exception);
            case "DELETE /api/v1/employees/1" -> doThrow(exception).when(employeeService).delete(1L);

            case "POST /api/v1/assignments" -> when(assignmentService.assign(any())).thenThrow(exception);
            case "GET /api/v1/assignments" ->
                    when(assignmentService.getAll(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/assignments/1" -> when(assignmentService.getById(1L)).thenThrow(exception);
            case "GET /api/v1/assignments/active" ->
                    when(assignmentService.getActive(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/assignments/asset/1" ->
                    when(assignmentService.getByAsset(eq(1L), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/assignments/employee/1" ->
                    when(assignmentService.getByEmployee(eq(1L), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/assignments/date-range" ->
                    when(assignmentService.getByDateRange(
                            eq(LocalDate.of(2026, 4, 1)),
                            eq(LocalDate.of(2026, 4, 30)),
                            endpointPageable()
                    )).thenThrow(exception);
            case "PATCH /api/v1/assignments/1/return" -> when(assignmentService.returnAsset(1L)).thenThrow(exception);
            case "PATCH /api/v1/assignments/1/transfer" ->
                    when(assignmentService.transfer(1L, 2L)).thenThrow(exception);
            case "DELETE /api/v1/assignments/1" -> doThrow(exception).when(assignmentService).delete(1L);

            case "POST /api/v1/maintenance" -> when(maintenanceService.create(any())).thenThrow(exception);
            case "GET /api/v1/maintenance" ->
                    when(maintenanceService.getAll(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/maintenance/1" -> when(maintenanceService.getById(1)).thenThrow(exception);
            case "GET /api/v1/maintenance/asset/1" ->
                    when(maintenanceService.getByAsset(eq(1L), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/maintenance/status/SCHEDULED" ->
                    when(maintenanceService.getByStatus(eq(MaintenanceStatus.SCHEDULED), endpointPageable()))
                            .thenThrow(exception);
            case "GET /api/v1/maintenance/type/REPAIR" ->
                    when(maintenanceService.getByType(eq(MaintenanceType.REPAIR), endpointPageable()))
                            .thenThrow(exception);
            case "GET /api/v1/maintenance/scheduled" ->
                    when(maintenanceService.getScheduled(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/maintenance/date-range" ->
                    when(maintenanceService.getByDateRange(
                            eq(LocalDate.of(2026, 5, 1)),
                            eq(LocalDate.of(2026, 5, 30)),
                            endpointPageable()
                    )).thenThrow(exception);
            case "PUT /api/v1/maintenance/1" -> when(maintenanceService.update(eq(1), any())).thenThrow(exception);
            case "PATCH /api/v1/maintenance/1/start" -> when(maintenanceService.start(1)).thenThrow(exception);
            case "PATCH /api/v1/maintenance/1/complete" -> when(maintenanceService.complete(1)).thenThrow(exception);
            case "PATCH /api/v1/maintenance/1/cancel" -> when(maintenanceService.cancel(1)).thenThrow(exception);
            case "DELETE /api/v1/maintenance/1" -> doThrow(exception).when(maintenanceService).delete(1);

            case "POST /api/v1/licenses" -> when(softwareLicenseService.create(any())).thenThrow(exception);
            case "GET /api/v1/licenses" ->
                    when(softwareLicenseService.getAll(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/licenses/1" -> when(softwareLicenseService.getById(1L)).thenThrow(exception);
            case "GET /api/v1/licenses/expiring-soon" ->
                    when(softwareLicenseService.getExpiringSoon(eq(30), endpointPageable())).thenThrow(exception);
            case "GET /api/v1/licenses/expired" ->
                    when(softwareLicenseService.getExpired(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/licenses/low-seats" ->
                    when(softwareLicenseService.getLowSeats(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/licenses/search" ->
                    when(softwareLicenseService.search(eq("IntelliJ"), endpointPageable())).thenThrow(exception);
            case "PUT /api/v1/licenses/1" ->
                    when(softwareLicenseService.update(eq(1L), any())).thenThrow(exception);
            case "PATCH /api/v1/licenses/1/assign" ->
                    when(softwareAssignmentService.assign(1L, 1L)).thenThrow(exception);
            case "PATCH /api/v1/licenses/1/revoke" ->
                    when(softwareAssignmentService.revoke(1L, 1L)).thenThrow(exception);
            case "PATCH /api/v1/licenses/1/deactivate" ->
                    when(softwareLicenseService.deactivate(1L)).thenThrow(exception);
            case "DELETE /api/v1/licenses/1" -> doThrow(exception).when(softwareLicenseService).delete(1L);

            case "GET /api/v1/dashboard/summary" -> when(dashboardService.getSummary()).thenThrow(exception);
            case "GET /api/v1/dashboard/type-breakdown" ->
                    when(dashboardService.getTypeBreakdown(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/dashboard/department-assets" ->
                    when(dashboardService.getDepartmentAssets(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/dashboard/warranty-alerts" ->
                    when(dashboardService.getWarrantyAlerts(any(Pageable.class))).thenThrow(exception);
            case "GET /api/v1/dashboard/license-alerts" ->
                    when(dashboardService.getLicenseAlerts()).thenThrow(exception);
            case "GET /api/v1/dashboard/maintenance-summary" ->
                    when(dashboardService.getMaintenanceSummary()).thenThrow(exception);
            case "GET /api/v1/dashboard/asset-value" -> when(dashboardService.getAssetValue()).thenThrow(exception);

            default -> throw new IllegalArgumentException("No exception stub for " + endpoint);
        }
    }

    private Pageable endpointPageable() {
        return any(Pageable.class);
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
        String key() {
            return method + " " + pathWithoutQuery();
        }

        String pathWithoutQuery() {
            int queryStart = path.indexOf('?');
            return queryStart == -1 ? path : path.substring(0, queryStart);
        }

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
