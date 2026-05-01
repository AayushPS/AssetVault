package com.Gemini.AssetVault.Model;

import com.Gemini.AssetVault.Model.Enum.LicenseType;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "software_licence",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "")
        }
)
public class SoftwareLicense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "software_name", nullable = false, length = 100)
    private String softwareName;

    @Column(name = "licence_key", nullable = false, unique = true, length = 100)
    private String licenceKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "licence_type", nullable = false, length = 20)
    private LicenseType licenseType;

    @Column(nullable = false, length = 100)
    private String vendor;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "used_seats", nullable = false)
    private Integer usedSeats;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

}
