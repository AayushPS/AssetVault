package com.Gemini.AssetVault.Model;

import com.Gemini.AssetVault.Model.Enum.LicenseType;
import jakarta.persistence.*;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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
    @Setter
    private String vendor;

    @Column(name = "total_seats", nullable = false)
    @Setter
    private Integer totalSeats;

    @Column(name = "used_seats", nullable = false)
    @Setter
    private Integer usedSeats;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost", precision = 10, scale = 2)
    private BigDecimal purchaseCost;

    @Setter
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    //assignedEmployees handled via Assignment superclass and Software Assignment together, it's many to many featrure remains intact via Assignment and Software Assignment working together

    @Column(name = "is_active")
    @Setter
    private Boolean isActive;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @Setter
    private LocalDateTime updatedAt;
}
