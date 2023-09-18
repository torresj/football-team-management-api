package com.torresj.footballteammanagementapi.services.impl;

import com.torresj.footballteammanagementapi.dtos.MatchDto;
import com.torresj.footballteammanagementapi.dtos.MatchPlayer;
import com.torresj.footballteammanagementapi.entities.MatchEntity;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.enums.PlayerMatchStatus;
import com.torresj.footballteammanagementapi.exceptions.*;
import com.torresj.footballteammanagementapi.repositories.MatchRepository;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.services.MatchService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

  private final MemberRepository memberRepository;
  private final MatchRepository matchRepository;

  @Value("${admin.user}")
  private final String adminUser;

  @Override
  public MatchDto get(long id) throws MatchNotFoundException {
    var match = matchRepository.findById(id).orElseThrow(() -> new MatchNotFoundException(id));
    return matchToDto(match);
  }

  @Override
  public MatchDto getNext() throws NextMatchException {
    var match =
        matchRepository.findByMatchDayAfter(LocalDate.now()).orElseThrow(NextMatchException::new);
    return matchToDto(match);
  }

  @Override
  public List<MatchDto> get() {
    return matchRepository.findAll(Sort.by(Sort.Direction.DESC, "matchDay")).stream()
        .map(this::matchToDto)
        .toList();
  }

  @Override
  public MatchDto create(LocalDate matchDay) throws MatchAlreadyExistsException {
    var match = matchRepository.findByMatchDayAfter(LocalDate.now());
    if (match.isPresent()) throw new MatchAlreadyExistsException(matchDay.toString());
    return matchToDto(
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(matchDay)
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(
                    memberRepository.findAll().stream()
                        .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                        .map(MemberEntity::getId).collect(Collectors.toSet()))
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build()));
  }

  @Override
  public void close(long id) throws MatchNotFoundException {
    var match = matchRepository.findById(id).orElseThrow(()->new MatchNotFoundException(id));
    match.setClosed(true);
    matchRepository.save(match);
  }

  @Override
  public void addPlayer(long matchId, PlayerMatchStatus status, String playerName) throws MemberNotFoundException, MatchNotFoundException {
    var match = matchRepository.findById(matchId).orElseThrow(()->new MatchNotFoundException(matchId));

    if(match.isClosed()) throw new MatchNotFoundException(matchId);

    if (playerName.split("\\.").length != 2) {
      throw new MemberNotFoundException(playerName);
    }

    var player =
            memberRepository
                    .findByNameAndSurname(playerName.split("\\.")[0], playerName.split("\\.")[1])
                    .orElseThrow(() -> new MemberNotFoundException(""));

    switch (status){
      case AVAILABLE -> {
        match.getConfirmedPlayers().add(player.getId());
        match.getNotAvailablePlayers().remove(player.getId());
        match.getUnConfirmedPlayers().remove(player.getId());
      }
      case NOT_AVAILABLE -> {
        match.getNotAvailablePlayers().add(player.getId());
        match.getConfirmedPlayers().remove(player.getId());
        match.getUnConfirmedPlayers().remove(player.getId());
      }
    }

  }

  @Override
  public void addPlayerToTeamA(long matchId, long playerId)
      throws MatchNotFoundException, MemberNotFoundException, PlayerUnavailableException {
    var match = matchRepository.findById(matchId).orElseThrow(()->new MatchNotFoundException(matchId));
    var player = memberRepository.findById(playerId).orElseThrow(()->new MemberNotFoundException(""));

    if(match.getNotAvailablePlayers().contains(playerId)) throw new PlayerUnavailableException();

    if(match.isClosed()) throw new MatchNotFoundException(matchId);

    match.getTeamAPlayers().add(player.getId());
    match.getTeamBPlayers().remove(player.getId());
  }

  @Override
  public void addPlayerToTeamB(long matchId, long playerId)
      throws MatchNotFoundException, MemberNotFoundException, PlayerUnavailableException {
    var match = matchRepository.findById(matchId).orElseThrow(()->new MatchNotFoundException(matchId));
    var player = memberRepository.findById(playerId).orElseThrow(()->new MemberNotFoundException(""));

    if(match.getNotAvailablePlayers().contains(playerId)) throw new PlayerUnavailableException();

    if(match.isClosed()) throw new MatchNotFoundException(matchId);

    match.getTeamBPlayers().add(player.getId());
    match.getTeamAPlayers().remove(player.getId());
  }

  @Override
  public void removePlayerFromTeamA(long matchId, long playerId) throws MatchNotFoundException {
    var match = matchRepository.findById(matchId).orElseThrow(()->new MatchNotFoundException(matchId));
    match.getTeamAPlayers().remove(playerId);
  }

  @Override
  public void removePlayerFromTeamB(long matchId, long playerId) throws MatchNotFoundException {
    var match = matchRepository.findById(matchId).orElseThrow(()->new MatchNotFoundException(matchId));
    match.getTeamBPlayers().remove(playerId);
  }

  @Override
  public void addGuestToTeamA(long matchId, String guest) throws MatchNotFoundException {
    var match = matchRepository.findById(matchId).orElseThrow(()->new MatchNotFoundException(matchId));
    match.getTeamAGuests().add(guest);
  }

  @Override
  public void addGuestToTeamB(long matchId, String guest) throws MatchNotFoundException {
    var match = matchRepository.findById(matchId).orElseThrow(()->new MatchNotFoundException(matchId));
    match.getTeamBGuests().add(guest);
  }

  @Override
  public void removeGuestFromTeamA(long matchId, String guest) throws MatchNotFoundException {
    var match = matchRepository.findById(matchId).orElseThrow(()->new MatchNotFoundException(matchId));
    match.getTeamAGuests().remove(guest);
  }

  @Override
  public void removeGuestFromTeamB(long matchId, String guest) throws MatchNotFoundException {
    var match = matchRepository.findById(matchId).orElseThrow(()->new MatchNotFoundException(matchId));
    match.getTeamBGuests().remove(guest);
  }

  @Override
  public void delete(long id) {
    matchRepository.deleteById(id);
  }

  private MatchPlayer getPlayer(long playerId) {
    var member = memberRepository.findById(playerId);
    return new MatchPlayer(
        playerId,
        member
            .map(memberEntity -> memberEntity.getName() + " " + memberEntity.getSurname())
            .orElse("Not found"));
  }

  private MatchDto matchToDto(MatchEntity entity) {
    return new MatchDto(
        entity.getId(),
        entity.getMatchDay(),
        entity.getConfirmedPlayers().stream().map(this::getPlayer).collect(Collectors.toSet()),
        entity.getUnConfirmedPlayers().stream().map(this::getPlayer).collect(Collectors.toSet()),
        entity.getNotAvailablePlayers().stream().map(this::getPlayer).collect(Collectors.toSet()),
        entity.getTeamAPlayers().stream().map(this::getPlayer).toList(),
        entity.getTeamBPlayers().stream().map(this::getPlayer).toList(),
        entity.getTeamAGuests(),
        entity.getTeamBGuests(),
        entity.isClosed());
  }
}
