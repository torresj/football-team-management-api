package com.torresj.footballteammanagementapi.dtos;

import java.util.List;
import java.util.Set;

public record MatchDto(
        long id,
        String matchDay,
        Set<MatchPlayerDto> confirmedPlayers,
        Set<MatchPlayerDto> unConfirmedPlayers,
        Set<MatchPlayerDto> notAvailablePlayers,
        List<MatchPlayerDto> teamAPlayers,
        List<MatchPlayerDto> teamBPlayers,
        List<String> teamAGuests,
        List<String> teamBGuests,
        MatchPlayerDto captainTeamA,
        MatchPlayerDto captainTeamB,
        boolean closed) {
}
