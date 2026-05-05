package com.assetvault.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoSchemaDocumentationTest {
    private static final List<Class<?>> DTO_TYPES = List.of(
            AssetAssignmentRequest.class,
            AssetAssignmentResponse.class,
            AssetRequest.class,
            AssetResponse.class,
            AssetValueResponse.class,
            DashboardSummaryResponse.class,
            DepartmentAssetSummaryResponse.class,
            EmployeeRequest.class,
            EmployeeResponse.class,
            ErrorResponse.class,
            LicenseAlertsResponse.class,
            MaintenanceRecordRequest.class,
            MaintenanceRecordResponse.class,
            MaintenanceSummaryResponse.class,
            SoftwareAssignmentResponse.class,
            SoftwareLicenseRequest.class,
            SoftwareLicenseResponse.class,
            TypeBreakdownResponse.class
    );

    @Test
    void dtoTypesAndFieldsHaveSchemaDescriptionsAndExamples() {
        for (Class<?> dtoType : DTO_TYPES) {
            Schema typeSchema = dtoType.getAnnotation(Schema.class);

            assertThat(typeSchema)
                    .as("%s should declare @Schema", dtoType.getSimpleName())
                    .isNotNull();
            assertThat(typeSchema.description())
                    .as("%s should document a schema description", dtoType.getSimpleName())
                    .isNotBlank();
            assertThat(typeSchema.example())
                    .as("%s should document a schema example", dtoType.getSimpleName())
                    .isNotBlank();

            for (RecordComponent component : dtoType.getRecordComponents()) {
                Schema fieldSchema = schemaForComponent(dtoType, component);

                assertThat(fieldSchema)
                        .as("%s.%s should declare @Schema", dtoType.getSimpleName(), component.getName())
                        .isNotNull();
                assertThat(fieldSchema.description())
                        .as("%s.%s should document a schema description", dtoType.getSimpleName(), component.getName())
                        .isNotBlank();
                assertThat(fieldSchema.example())
                        .as("%s.%s should document a schema example", dtoType.getSimpleName(), component.getName())
                        .isNotBlank();
            }
        }
    }

    private Schema schemaForComponent(Class<?> dtoType, RecordComponent component) {
        Schema componentSchema = component.getAnnotation(Schema.class);
        if (componentSchema != null) {
            return componentSchema;
        }

        Schema accessorSchema = component.getAccessor().getAnnotation(Schema.class);
        if (accessorSchema != null) {
            return accessorSchema;
        }

        try {
            Field field = dtoType.getDeclaredField(component.getName());
            return field.getAnnotation(Schema.class);
        } catch (NoSuchFieldException ex) {
            return null;
        }
    }
}
