package com.torresj.footballteammanagementapi.services;

import com.torresj.footballteammanagementapi.dtos.MatchDto;
import com.torresj.footballteammanagementapi.enums.PlayerMatchStatus;
import com.torresj.footballteammanagementapi.exceptions.*;

import java.time.LocalDate;
import java.util.List;

public interface MatchService {
  MatchDto get(long id) throws MatchNotFoundException;

  MatchDto getNext() throws NextMatchException;

  List<MatchDto> get();

  MatchDto create(LocalDate matchDay) throws MatchAlreadyExistsException;

  void close(long id) throws MatchNotFoundException;

  void addPlayer(long matchId, PlayerMatchStatus status, String playerName)
      throws MemberNotFoundException, MatchNotFoundException, MemberBlockedException;

  void addPlayerToTeamA(long matchId, long playerId)
      throws MatchNotFoundException, MemberNotFoundException, PlayerUnavailableException;

  void addPlayerToTeamB(long matchId, long playerId)
      throws MatchNotFoundException, MemberNotFoundException, PlayerUnavailableException;

  void removePlayerFromTeamA(long matchId, long playerId) throws MatchNotFoundException;

  void removePlayerFromTeamB(long matchId, long playerId) throws MatchNotFoundException;

  void addGuestToTeamA(long matchId, String guest) throws MatchNotFoundException;

  void addGuestToTeamB(long matchId, String guest) throws MatchNotFoundException;

  void removeGuestFromTeamA(long matchId, String guest) throws MatchNotFoundException;

  void removeGuestFromTeamB(long matchId, String guest) throws MatchNotFoundException;

  void setRandomCaptainTeamA(long matchId) throws MatchNotFoundException;

  void setRandomCaptainTeamB(long matchId) throws MatchNotFoundException;

  void delete(long id);

  void closePastMatches();
}
