package com.assetvault.controller;

import com.assetvault.dto.AssetAssignmentResponse;
import com.assetvault.dto.AssetResponse;
import com.assetvault.dto.EmployeeRequest;
import com.assetvault.dto.EmployeeResponse;
import com.assetvault.dto.SoftwareLicenseResponse;
import com.assetvault.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employees")
@Tag(name = "Employees", description = "Employee registration, search, assignment history, and deactivation APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register employee", description = "Creates an employee and auto-generates a code when omitted.")
    public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request) {
        return employeeService.create(request);
    }

    @GetMapping
    @Operation(summary = "Get all employees", description = "Returns a paginated and sortable list of employees.")
    public Page<EmployeeResponse> getAll(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return employeeService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Returns an employee by database id.")
    public EmployeeResponse getById(@PathVariable @Positive Long id) {
        return employeeService.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search employees by name", description = "Searches employees by partial name.")
    public Page<EmployeeResponse> search(
            @RequestParam @NotBlank String name,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return employeeService.searchByName(name, pageable);
    }

    @GetMapping("/department")
    @Operation(summary = "Get employees by department", description = "Returns employees in a department.")
    public Page<EmployeeResponse> byDepartment(
            @RequestParam @NotBlank String name,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return employeeService.getByDepartment(name, pageable);
    }

    @GetMapping("/{id}/assets")
    @Operation(summary = "Get employee assets", description = "Returns assets actively assigned to an employee.")
    public Page<AssetResponse> assignedAssets(
            @PathVariable @Positive Long id,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return employeeService.getAssignedAssets(id, pageable);
    }

    @GetMapping("/{id}/licenses")
    @Operation(summary = "Get employee licenses", description = "Returns software licenses actively assigned to an employee.")
    public Page<SoftwareLicenseResponse> assignedLicenses(
            @PathVariable @Positive Long id,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return employeeService.getAssignedLicenses(id, pageable);
    }

    @GetMapping("/{id}/assignment-history")
    @Operation(summary = "Get employee assignment history", description = "Returns full asset assignment history for an employee.")
    public Page<AssetAssignmentResponse> assignmentHistory(
            @PathVariable @Positive Long id,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        return employeeService.getAssignmentHistory(id, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Updates employee profile details.")
    public EmployeeResponse update(@PathVariable @Positive Long id, @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate employee", description = "Deactivates an employee after checking active assets.")
    public EmployeeResponse deactivate(@PathVariable @Positive Long id) {
        return employeeService.deactivate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete employee", description = "Deletes an employee record.")
    public void delete(@PathVariable @Positive Long id) {
        employeeService.delete(id);
    }
}
