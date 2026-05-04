🖥️ AssetVault
  PROBLEM STATEMENT




Internal Asset Management System

  ☕ Java 21          🍃 Spring Boot 3.3+             🗄️ H2 In-Memory DB              📋 Maven          📖 OpenAPI 2.x


  📌 Overview
You are required to build AssetVault, a backend REST API system for a company's IT/Admin department that manages the complete
lifecycle of internal company assets — from procurement and assignment to maintenance, transfers, and retirement.

This system will serve as the backend for the IT admin portal, enabling asset managers and department heads to track every laptop,
monitor, software license, and peripheral assigned across the organization through well-documented, production-ready APIs.




  🛠️ Technical Guidelines                    (April 2026)



  FRAMEWORK                                                 LANGUAGE                                                  BUILD TOOL

  Spring Boot 3.3+                                          Java 21 (LTS)                                             Maven — proper pom.xml



  DATABASE                                                  ORM                                                       API DOCS

  H2 In-Memory (dev profile)                                Spring Data JPA + Hibernate                               SpringDoc OpenAPI 2.x — /swagger-
                                                                                                                      ui.html



  VALIDATION                                                TESTING                                                   LOGGING

  Jakarta Bean Validation                                   JUnit 5 + Mockito + @WebMvcTest +                         SLF4J + Logback
                                                            @DataJpaTest



  EXCEPTION HANDLING                                        CODE QUALITY

  @RestControllerAdvice + custom                            SOLID principles — DTOs mandatory
  exceptions




  📦 Required Package Structure
  ⚠️ Entities must NOT be exposed directly in API responses — always use DTOs.

   com.assetvault
   ├── config/                        # SwaggerConfig, AppConfig
   ├── controller/                 # AssetController, EmployeeController, AssignmentController
   │                              # MaintenanceController, LicenseController
   ├── service/                       # interfaces + impl classes
   ├── repository/                 # JPA Repository interfaces
   ├── model/                         # JPA Entity classes
   ├── dto/                           # Request DTOs + Response DTOs
   ├── exception/                  # Custom exceptions + GlobalExceptionHandler
   └── util/                       # Constants, AssetCodeGenerator, helpers




  🗂️ Domain & Entities
   Asset                                                     Employee                                                  AssetAssignment

   id, assetCode (unique, auto-generated),                   id, name, email, department, designation,                 id, asset (ManyToOne), employee
   name, brand, model, type (LAPTOP /                        employeeCode, isActive, createdAt                         (ManyToOne), assignedDate, returnedDate,
   DESKTOP / MONITOR / KEYBOARD /                                                                                      status (ACTIVE / RETURNED /
   MOUSE / HEADSET / PHONE / PRINTER /                                                                                 TRANSFERRED), assignedBy, remarks,
   OTHER), status (AVAILABLE / ASSIGNED /                                                                              createdAt
   UNDER_MAINTENANCE / RETIRED /
   LOST), purchaseDate, purchaseCost,
   warrantyExpiryDate, serialNumber, location,
   notes, createdAt, updatedAt




   MaintenanceRecord                                         SoftwareLicense

   id, asset (ManyToOne), maintenanceType                    id, softwareName, licenseKey, licenseType
   (REPAIR / SERVICE / UPGRADE /                             (INDIVIDUAL / FLOATING / SITE), vendor,
   INSPECTION), description,                                 totalSeats, usedSeats, purchaseDate,
   maintenanceCost, vendor, scheduledDate,                   expiryDate, assignedEmployees
   completedDate, status (SCHEDULED /                        (ManyToMany), isActive, createdAt,
   IN_PROGRESS / COMPLETED /                                 updatedAt
   CANCELLED), createdAt




  🔌 REST API Requirements
  🖥️ Asset Module — /api/v1/assets
  Method                  Endpoint                                                  Description


   POST                   /                                                         Register a new asset


   GET                    /                                                         Get all assets (paginated + sorted)


   GET                    /{id}                                                     Get a single asset by ID


   GET                    /code/{assetCode}                                         Get asset by its unique asset code


   GET                    /type/{type}                                              Get assets filtered by type


   GET                    /status/{status}                                          Get assets filtered by status


   GET                    /available                                                Get all currently available (unassigned) assets


   GET                    /department?name=                                         Get assets currently assigned in a department


   GET                    /warranty-expiring?days=                                  Get assets whose warranty expires within N days


   GET                    /warranty-expired                                         Get all assets with expired warranty


   GET                    /search?keyword=                                          Search assets by name, brand, or model


   GET                    /low-value?maxCost=                                       Get assets below a certain purchase cost


   PUT                    /{id}                                                     Update full asset details


   PATCH                  /{id}/status?status=                                      Update only the asset status


   PATCH                  /{id}/retire                                              Mark an asset as retired


   PATCH                  /{id}/mark-lost                                           Mark an asset as lost


   DELETE                 /{id}                                                     Delete an asset record




  👤 Employee Module — /api/v1/employees
  Method               Endpoint                                                 Description


   POST                /                                                        Register a new employee


   GET                 /                                                        Get all employees (paginated)


   GET                 /{id}                                                    Get employee by ID


   GET                 /search?name=                                            Search employee by name


   GET                 /department?name=                                        Get employees by department


   GET                 /{id}/assets                                             Get all assets assigned to an employee


   GET                 /{id}/licenses                                           Get all software licenses assigned to an employee


   GET                 /{id}/assignment-history                                 Get full asset assignment history of an employee


   PUT                 /{id}                                                    Update employee details


   PATCH               /{id}/deactivate                                         Deactivate an employee (triggers asset recall check)


   DELETE              /{id}                                                    Delete an employee record




  🔁 Assignment Module — /api/v1/assignments
  Method              Endpoint                                                        Description


   POST               /                                                               Assign an asset to an employee


   GET                /                                                               Get all assignments (paginated)


   GET                /{id}                                                           Get assignment by ID


   GET                /active                                                         Get all currently active assignments


   GET                /asset/{assetId}                                                Get full assignment history for an asset


   GET                /employee/{employeeId}                                          Get full assignment history for an employee


   GET                /date-range?from=&to=                                           Get assignments within a date range


   PATCH              /{id}/return                                                    Return an asset — marks assignment as RETURNED


   PATCH              /{id}/transfer?toEmployeeId=                                    Transfer asset from one employee to another


   DELETE             /{id}                                                           Delete an assignment record




  🔧 Maintenance Module — /api/v1/maintenance
  Method                      Endpoint                                              Description


   POST                       /                                                     Create a maintenance record for an asset


   GET                        /                                                     Get all maintenance records (paginated)


   GET                        /{id}                                                 Get maintenance record by ID


   GET                        /asset/{assetId}                                      Get all maintenance records for an asset


   GET                        /status/{status}                                      Get maintenance records by status


   GET                        /type/{type}                                          Get maintenance records by type


   GET                        /scheduled                                            Get all upcoming scheduled maintenance


   GET                        /date-range?from=&to=                                 Get maintenance records within a date range


   PUT                        /{id}                                                 Update maintenance record details


   PATCH                      /{id}/complete                                        Mark maintenance as completed


   PATCH                      /{id}/cancel                                          Cancel a scheduled maintenance


   DELETE                     /{id}                                                 Delete a maintenance record




  🔑 Software License Module — /api/v1/licenses
  Method                      Endpoint                                                         Description


   POST                       /                                                                Add a new software license


   GET                        /                                                                Get all licenses (paginated)


   GET                        /{id}                                                            Get license by ID


   GET                        /expiring-soon?days=                                             Get licenses expiring within N days


   GET                        /expired                                                         Get all expired licenses


   GET                        /low-seats                                                       Get licenses with no remaining seats


   GET                        /search?name=                                                    Search licenses by software name


   PUT                        /{id}                                                            Update license details


   PATCH                      /{id}/assign?employeeId=                                         Assign a license seat to an employee


   PATCH                      /{id}/revoke?employeeId=                                         Revoke a license from an employee


   PATCH                      /{id}/deactivate                                                 Deactivate a license


   DELETE                     /{id}                                                            Delete a license record




  📊 Dashboard Module — /api/v1/dashboard
  Method          Endpoint                                      Description


   GET            /summary                                      Total assets, assigned, available, under maintenance, retired counts


   GET            /type-breakdown                               Asset count grouped by type


   GET            /department-assets                            Asset count and value per department


   GET            /warranty-alerts                              Assets with warranty expiring in the next 30 days


   GET            /license-alerts                               Licenses expiring soon + seats exhausted


   GET            /maintenance-summary                          Scheduled, in-progress, and completed maintenance counts


   GET            /asset-value                                  Total purchase cost of all active assets




  ⚠️ Exception Handling Requirements
All exceptions must be handled in a single GlobalExceptionHandler using @RestControllerAdvice.


  AssetNotFoundException                                    EmployeeNotFoundException                                  AssignmentNotFoundException
  404 Not Found                                             404 Not Found                                              404 Not Found

  Asset ID or code doesn't exist                            Employee ID doesn't exist                                  Assignment ID doesn't exist



  MaintenanceRecordNotFoundException                        LicenseNotFoundException                                   AssetNotAvailableException
  404 Not Found                                             404 Not Found                                              409 Conflict

  Maintenance record ID doesn't exist                       License ID doesn't exist                                   Assigning an asset not in AVAILABLE status



  AssetRetiredException                                     AssetAlreadyAssignedException                              InactiveEmployeeException
  409 Conflict                                              409 Conflict                                               409 Conflict

  Any operation on a RETIRED asset                          Asset is already actively assigned                         Assigning asset/license to a deactivated
                                                                                                                       employee



  NoLicenseSeatsAvailableException                          LicenseExpiredException                                    LicenseAlreadyAssignedException
  409 Conflict                                              422 Unprocessable Entity                                   409 Conflict

  All seats for a license are already used                  Assigning an expired software license                      Employee already holds this license



  EmployeeHasActiveAssetsException                          DuplicateSerialNumberException                             MethodArgumentNotValidException
  409 Conflict                                              409 Conflict                                               400 Bad Request

  Deactivating employee with unreturned assets              Serial number already exists in the system                 Bean validation failure on request body



  ConstraintViolationException
  400 Bad Request

  Validation failure on query/path params




   {
       "timestamp": "2026-04-21T09:45:00",
       "status": 409,
       "error": "Conflict",
       "message": "Asset LPT-00123 is currently ASSIGNED and cannot be reassigned without returning it first",
       "path": "/api/v1/assignments"
   }




  ✅ Unit Testing Requirements
  Layer                Approach                                                     What to Cover


  Controller           @WebMvcTest + MockMvc + Mockito                              All endpoints — success + exception scenarios


  Service              @ExtendWith(MockitoExtension.class)                          All business logic, edge cases, exception throws


  Repository           @DataJpaTest                                                 Custom query methods, status/type filters, date-range queries



  📊 Minimum expected coverage: 80% on controller and service layers. Use @ParameterizedTest where multiple input scenarios apply.


  📝 Logging Requirements
  ❌ System.out.println is strictly NOT allowed anywhere in the codebase.
  Level                       When to Log


  INFO                        Asset registered, assigned, returned, transferred, retired


  WARN                        Warranty/license expiry approaching, seat limit nearing exhaustion


  ERROR                       Exception caught in global handler, unexpected failures


  DEBUG                       Method entry/exit in service layer (dev profile only)




  📖 Swagger / OpenAPI Requirements
  Swagger UI accessible at /swagger-ui.html

  All controllers annotated with @Tag(name = ..., description = ...)

  All endpoints annotated with @Operation(summary = ..., description = ...)

  All DTOs annotated with @Schema(description = ..., example = ...)

  APIs visually grouped by module in Swagger UI




  🚀 Bonus (Optional but Impressive)
  ☐ Spring Profiles — dev (H2) and prod                     ☐ Spring Boot Actuator — /actuator/health                 ☐ Pagination + Sorting on all list endpoints
       (MySQL/PostgreSQL)                                      and /actuator/info                                        using Pageable



  ☐ Auto-generate unique assetCode (e.g.,                   ☐ Add updatedAt auto-tracking using                       ☐ Block deactivation of employee with active
       LPT-00123, MNT-00045)                                   @LastModifiedDate                                         asset assignments




  📬 Deliverable
   A fully working Maven project pushed to a GitHub repository with a well-written README.md that includes setup instructions, API
   overview table, H2 console access steps, and the Swagger UI link.
