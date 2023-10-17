package com.torresj.footballteammanagementapi.entities;

import com.torresj.footballteammanagementapi.enums.MovementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
public class MovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false)
    private MovementType type;

    @Column(nullable = false,updatable = false)
    private long memberId;

    @Column(nullable = false)
    private double amount;

    @Column
    private String description;

    @CreationTimestamp
    private LocalDate createdOn;
}
