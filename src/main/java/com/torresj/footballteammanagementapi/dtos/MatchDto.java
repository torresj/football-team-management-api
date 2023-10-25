package com.torresj.footballteammanagementapi.dtos;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record MatchDto(
    long id,
    String matchDay,
    Set<MatchPlayer> confirmedPlayers,
    Set<MatchPlayer> unConfirmedPlayers,
    Set<MatchPlayer> notAvailablePlayers,
    List<MatchPlayer> teamAPlayers,
    List<MatchPlayer> teamBPlayers,
    List<String> teamAGuests,
    List<String> teamBGuests,
    boolean closed) {}
