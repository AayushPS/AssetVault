package com.Gemini.AssetVault.Model;

import com.Gemini.AssetVault.Model.Enum.LicenseType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "software_license",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "license_key")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoftwareLicense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "software_name", nullable = false, length = 100)
    private String softwareName;

    @Column(name = "license_key", nullable = false, unique = true, length = 100)
    private String licenceKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false, length = 20)
    private LicenseType licenseType;

    @Column(nullable = false, length = 100)
    private String vendor;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "used_seats", nullable = false)
    @Builder.Default
    private Integer usedSeats = 0;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost", precision = 10, scale = 2)
    private BigDecimal purchaseCost;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    //assignedEmployees handled via Assignment superclass and Software Assignment together, it's many to many featrure remains intact via Assignment and Software Assignment working together

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
