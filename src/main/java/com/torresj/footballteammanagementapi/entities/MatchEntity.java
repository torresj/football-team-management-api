package com.torresj.footballteammanagementapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

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

    @Temporal(TemporalType.DATE)
    private Date matchDay;

    @ElementCollection(targetClass = Long.class)
    private List<Long> players;

    @ElementCollection(targetClass = Long.class)
    private List<Long> teamAPlayers;

    @ElementCollection(targetClass = Long.class)
    private List<Long> teamABPlayers;

    @ElementCollection(targetClass = String.class)
    private List<String> teamAGuests;

    @ElementCollection(targetClass = String.class)
    private List<String> teamBGuests;

    @Column
    private boolean closed;
}
