package com.campus.lostfound.entity;

import com.campus.lostfound.entity.enums.ClaimStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Represents a claim submitted by a student to verify ownership of a found item report.
 */
@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Item report is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemReport itemReport;

    @NotNull(message = "Claimant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimant_id", nullable = false)
    private User claimant;

    @NotBlank(message = "Claim description is required")
    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @NotNull(message = "Claim date is required")
    @Column(name = "claim_date", nullable = false)
    private LocalDate claimDate;

    @NotNull(message = "Claim status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "claim_status", nullable = false, length = 20)
    private ClaimStatus claimStatus;

    // Default no-argument constructor required by JPA
    public Claim() {
    }

    // Parameterized constructor
    public Claim(ItemReport itemReport, User claimant, String description,
                 LocalDate claimDate, ClaimStatus claimStatus) {
        this.itemReport = itemReport;
        this.claimant = claimant;
        this.description = description;
        this.claimDate = claimDate;
        this.claimStatus = claimStatus;
    }

    // Full constructor
    public Claim(Long id, ItemReport itemReport, User claimant, String description,
                 LocalDate claimDate, ClaimStatus claimStatus) {
        this.id = id;
        this.itemReport = itemReport;
        this.claimant = claimant;
        this.description = description;
        this.claimDate = claimDate;
        this.claimStatus = claimStatus;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemReport getItemReport() {
        return itemReport;
    }

    public void setItemReport(ItemReport itemReport) {
        this.itemReport = itemReport;
    }

    public User getClaimant() {
        return claimant;
    }

    public void setClaimant(User claimant) {
        this.claimant = claimant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }

    public ClaimStatus getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(ClaimStatus claimStatus) {
        this.claimStatus = claimStatus;
    }
}

