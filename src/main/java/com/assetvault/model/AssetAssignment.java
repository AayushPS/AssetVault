package com.assetvault.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * JPA entity representing asset assignment.
 */
@Entity
@Table(name = "asset_assignment")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AssetAssignment extends Assignment{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;
}
