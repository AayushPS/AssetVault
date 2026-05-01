package com.Gemini.AssetVault.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asset_assignment")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetAssignment extends Assignment{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;
}
