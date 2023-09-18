package com.torresj.footballteammanagementapi.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
public class MatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(columnDefinition = "DATE")
    private LocalDate matchDay;

    @ElementCollection(targetClass = Long.class)
    private Set<Long> confirmedPlayers;

    @ElementCollection(targetClass = Long.class)
    private Set<Long> unConfirmedPlayers;

    @ElementCollection(targetClass = Long.class)
    private Set<Long> notAvailablePlayers;

    @ElementCollection(targetClass = Long.class)
    private List<Long> teamAPlayers;

    @ElementCollection(targetClass = Long.class)
    private List<Long> teamBPlayers;

    @ElementCollection(targetClass = String.class)
    private List<String> teamAGuests;

    @ElementCollection(targetClass = String.class)
    private List<String> teamBGuests;

    @Column
    @Setter
    private boolean closed;
}
