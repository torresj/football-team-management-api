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

    @Column
    private Long captainTeamA;

    @Column
    private Long captainTeamB;

    @ElementCollection(targetClass = Long.class, fetch = FetchType.EAGER)
    private Set<Long> confirmedPlayers;

    @ElementCollection(targetClass = Long.class, fetch = FetchType.EAGER)
    private Set<Long> unConfirmedPlayers;

    @ElementCollection(targetClass = Long.class, fetch = FetchType.EAGER)
    private Set<Long> notAvailablePlayers;

    @ElementCollection(targetClass = Long.class, fetch = FetchType.EAGER)
    private List<Long> teamAPlayers;

    @ElementCollection(targetClass = Long.class, fetch = FetchType.EAGER)
    private List<Long> teamBPlayers;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    private List<String> teamAGuests;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    private List<String> teamBGuests;

    @Column
    @Setter
    private boolean closed;
}
