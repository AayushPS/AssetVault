package com.Gemini.AssetVault.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "software_assignment",
    uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"employee_id","licence_id"}
        )
    }
)
public class SoftwareAssignment extends Assignment{
}
