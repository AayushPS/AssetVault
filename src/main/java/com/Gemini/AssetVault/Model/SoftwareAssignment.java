package com.Gemini.AssetVault.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "software_assignment",
    uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"employee_id","licence_id"}
        )
    }
)
public class SoftwareAssignment extends Assignment{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "license_id", nullable = false)
    private SoftwareLicense softwareLicense;

    @Setter
    @Column(name = "seat_index")
    private Integer seatIndex;
}
