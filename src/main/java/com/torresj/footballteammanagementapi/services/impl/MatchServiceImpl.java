package com.torresj.footballteammanagementapi.services.impl;

import com.torresj.footballteammanagementapi.dtos.MatchDto;
import com.torresj.footballteammanagementapi.dtos.MatchPlayerDto;
import com.torresj.footballteammanagementapi.entities.MatchEntity;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.entities.MovementEntity;
import com.torresj.footballteammanagementapi.enums.MovementType;
import com.torresj.footballteammanagementapi.enums.PlayerMatchStatus;
import com.torresj.footballteammanagementapi.exceptions.*;
import com.torresj.footballteammanagementapi.repositories.MatchRepository;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.repositories.MovementRepository;
import com.torresj.footballteammanagementapi.services.MatchService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

  private final MemberRepository memberRepository;
  private final MatchRepository matchRepository;
  private final MovementRepository movementRepository;

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
        matchRepository
            .findByMatchDayGreaterThanEqual(LocalDate.now())
            .orElseThrow(NextMatchException::new);
    return matchToDto(match);
  }

  @Override
  public List<MatchDto> get() {
    return matchRepository.findAll(Sort.by(Sort.Direction.DESC, "matchDay")).stream()
        .map(this::matchToDto)
        .filter(MatchDto::closed)
        .toList();
  }

  @Override
  public MatchDto create(LocalDate matchDay) throws MatchAlreadyExistsException {
    var match = matchRepository.findByMatchDayGreaterThanEqual(LocalDate.now());
    if (match.isPresent()) throw new MatchAlreadyExistsException(matchDay.toString());
    return matchToDto(
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(matchDay)
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(
                    memberRepository.findAll().stream()
                        .filter(memberEntity -> !adminUser.equals(memberEntity.getName()))
                        .map(MemberEntity::getId)
                        .collect(Collectors.toSet()))
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build()));
  }

  @Override
  public void close(long id) throws MatchNotFoundException {
    var match = matchRepository.findById(id).orElseThrow(() -> new MatchNotFoundException(id));
    match.setClosed(true);
    matchRepository.save(match);
    Stream.concat(match.getNotAvailablePlayers().stream(), match.getUnConfirmedPlayers().stream())
        .map(memberRepository::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(member -> !member.isInjured())
        .map(
            member ->
                MovementEntity.builder()
                    .type(MovementType.EXPENSE)
                    .amount(-1)
                    .description(
                        "Multa por no ir al partido del "
                            + DateTimeFormatter.ofPattern("dd/MM/yy").format(match.getMatchDay()))
                    .memberId(member.getId())
                    .build())
        .forEach(movementRepository::save);

    if (match.getCaptainTeamA() != null) {
      var captainA = memberRepository.findById(match.getCaptainTeamA());
      captainA.ifPresent(
          member ->
              memberRepository.save(
                  MemberEntity.builder()
                      .id(member.getId())
                      .nonce(member.getNonce())
                      .phone(member.getPhone())
                      .name(member.getName())
                      .surname(member.getSurname())
                      .injured(member.isInjured())
                      .password(member.getPassword())
                      .role(member.getRole())
                      .nCaptaincies(member.getNCaptaincies() + 1)
                      .build()));
    }

    if (match.getCaptainTeamB() != null) {
      var captainB = memberRepository.findById(match.getCaptainTeamB());
      captainB.ifPresent(
          member ->
              memberRepository.save(
                  MemberEntity.builder()
                      .id(member.getId())
                      .nonce(member.getNonce())
                      .phone(member.getPhone())
                      .name(member.getName())
                      .surname(member.getSurname())
                      .injured(member.isInjured())
                      .password(member.getPassword())
                      .role(member.getRole())
                      .nCaptaincies(member.getNCaptaincies() + 1)
                      .build()));
    }
  }

  @Override
  public void addPlayer(long matchId, PlayerMatchStatus status, String playerName)
      throws MemberNotFoundException, MatchNotFoundException, MemberBlockedException {
    var match =
        matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));

    if (match.isClosed()) throw new MatchNotFoundException(matchId);

    if (playerName.split("\\.").length != 2) {
      throw new MemberNotFoundException(playerName);
    }

    var player =
        memberRepository
            .findByNameAndSurname(playerName.split("\\.")[0], playerName.split("\\.")[1])
            .orElseThrow(() -> new MemberNotFoundException(""));

    if (player.isBlocked()) throw new MemberBlockedException(playerName);

    switch (status) {
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

    matchRepository.save(match);
  }

  @Override
  public void addPlayerToTeamA(long matchId, long playerId)
      throws MatchNotFoundException, MemberNotFoundException, PlayerUnavailableException {
    var match =
        matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
    var player =
        memberRepository.findById(playerId).orElseThrow(() -> new MemberNotFoundException(""));

    if (match.getNotAvailablePlayers().contains(playerId)
        || match.getUnConfirmedPlayers().contains(playerId)
        || !match.getConfirmedPlayers().contains(playerId)) {
      throw new PlayerUnavailableException();
    }

    if (match.isClosed()) throw new MatchNotFoundException(matchId);

    match.getTeamAPlayers().add(player.getId());
    match.getTeamBPlayers().remove(player.getId());

    matchRepository.save(match);
  }

  @Override
  public void addPlayerToTeamB(long matchId, long playerId)
      throws MatchNotFoundException, MemberNotFoundException, PlayerUnavailableException {
    var match =
        matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
    var player =
        memberRepository.findById(playerId).orElseThrow(() -> new MemberNotFoundException(""));

    if (match.getNotAvailablePlayers().contains(playerId)
        || match.getUnConfirmedPlayers().contains(playerId)
        || !match.getConfirmedPlayers().contains(playerId)) {
      throw new PlayerUnavailableException();
    }

    if (match.isClosed()) throw new MatchNotFoundException(matchId);

    match.getTeamBPlayers().add(player.getId());
    match.getTeamAPlayers().remove(player.getId());

    matchRepository.save(match);
  }

  @Override
  public void removePlayerFromTeamA(long matchId, long playerId) throws MatchNotFoundException {
    var match =
        matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
    match.getTeamAPlayers().remove(playerId);
    if (match.getCaptainTeamA() != null && match.getCaptainTeamA() == playerId) {
      matchRepository.save(
          MatchEntity.builder()
              .id(match.getId())
              .matchDay(match.getMatchDay())
              .confirmedPlayers(match.getConfirmedPlayers())
              .notAvailablePlayers(match.getNotAvailablePlayers())
              .unConfirmedPlayers(match.getUnConfirmedPlayers())
              .teamAPlayers(match.getTeamAPlayers())
              .teamBPlayers(match.getTeamBPlayers())
              .teamAGuests(match.getTeamAGuests())
              .teamBGuests(match.getTeamBGuests())
              .captainTeamA(null)
              .captainTeamB(match.getCaptainTeamB())
              .closed(match.isClosed())
              .build());
    } else {
      matchRepository.save(match);
    }
  }

  @Override
  public void removePlayerFromTeamB(long matchId, long playerId) throws MatchNotFoundException {
    var match =
        matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
    match.getTeamBPlayers().remove(playerId);
    if (match.getCaptainTeamB() != null && match.getCaptainTeamB() == playerId) {
      matchRepository.save(
          MatchEntity.builder()
              .id(match.getId())
              .matchDay(match.getMatchDay())
              .confirmedPlayers(match.getConfirmedPlayers())
              .notAvailablePlayers(match.getNotAvailablePlayers())
              .unConfirmedPlayers(match.getUnConfirmedPlayers())
              .teamAPlayers(match.getTeamAPlayers())
              .teamBPlayers(match.getTeamBPlayers())
              .teamAGuests(match.getTeamAGuests())
              .teamBGuests(match.getTeamBGuests())
              .captainTeamB(null)
              .captainTeamA(match.getCaptainTeamA())
              .closed(match.isClosed())
              .build());
    } else {
      matchRepository.save(match);
    }
  }

  @Override
  public void addGuestToTeamA(long matchId, String guest) throws MatchNotFoundException {
    var match =
        matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
    match.getTeamAGuests().add(guest);

    matchRepository.save(match);
  }

  @Override
  public void addGuestToTeamB(long matchId, String guest) throws MatchNotFoundException {
    var match =
        matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
    match.getTeamBGuests().add(guest);

    matchRepository.save(match);
  }

  @Override
  public void removeGuestFromTeamA(long matchId, String guest) throws MatchNotFoundException {
    var match =
        matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
    match.getTeamAGuests().remove(guest);

    matchRepository.save(match);
  }

  @Override
  public void removeGuestFromTeamB(long matchId, String guest) throws MatchNotFoundException {
    var match =
        matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
    match.getTeamBGuests().remove(guest);

    matchRepository.save(match);
  }

  @Override
  public void setRandomCaptainTeamA(long matchId) throws MatchNotFoundException {
    var match =
        matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));

    if (match.getTeamAPlayers().isEmpty()) {
      return;
    }

    int minimumCaptaincies =
        match.getTeamAPlayers().stream()
            .map(memberRepository::findById)
            .filter(Optional::isPresent)
            .map(memberEntity -> memberEntity.get().getNCaptaincies())
            .min(Comparator.naturalOrder())
            .orElse(0);

    var listOfPossibleCaptains =
        match.getTeamAPlayers().stream()
            .map(memberRepository::findById)
            .filter(Optional::isPresent)
            .filter(memberEntity -> memberEntity.get().getNCaptaincies() == minimumCaptaincies)
            .map(Optional::get)
            .toList();

    matchRepository.save(
        MatchEntity.builder()
            .id(match.getId())
            .matchDay(match.getMatchDay())
            .confirmedPlayers(match.getConfirmedPlayers())
            .notAvailablePlayers(match.getNotAvailablePlayers())
            .unConfirmedPlayers(match.getUnConfirmedPlayers())
            .teamAPlayers(match.getTeamAPlayers())
            .teamBPlayers(match.getTeamBPlayers())
            .teamAGuests(match.getTeamAGuests())
            .teamBGuests(match.getTeamBGuests())
            .captainTeamA(
                listOfPossibleCaptains
                    .get(new Random().nextInt(listOfPossibleCaptains.size()))
                    .getId())
            .captainTeamB(match.getCaptainTeamB())
            .closed(match.isClosed())
            .build());
  }

  @Override
  public void setRandomCaptainTeamB(long matchId) throws MatchNotFoundException {
    var match =
        matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));

    if (match.getTeamBPlayers().isEmpty()) {
      return;
    }

    int minimumCaptaincies =
        match.getTeamBPlayers().stream()
            .map(memberRepository::findById)
            .filter(Optional::isPresent)
            .map(memberEntity -> memberEntity.get().getNCaptaincies())
            .min(Comparator.naturalOrder())
            .orElse(0);

    var listOfPossibleCaptains =
        match.getTeamBPlayers().stream()
            .map(memberRepository::findById)
            .filter(Optional::isPresent)
            .filter(memberEntity -> memberEntity.get().getNCaptaincies() == minimumCaptaincies)
            .map(Optional::get)
            .toList();

    matchRepository.save(
        MatchEntity.builder()
            .id(match.getId())
            .matchDay(match.getMatchDay())
            .confirmedPlayers(match.getConfirmedPlayers())
            .notAvailablePlayers(match.getNotAvailablePlayers())
            .unConfirmedPlayers(match.getUnConfirmedPlayers())
            .teamAPlayers(match.getTeamAPlayers())
            .teamBPlayers(match.getTeamBPlayers())
            .teamAGuests(match.getTeamAGuests())
            .teamBGuests(match.getTeamBGuests())
            .captainTeamB(
                listOfPossibleCaptains
                    .get(new Random().nextInt(listOfPossibleCaptains.size()))
                    .getId())
            .captainTeamA(match.getCaptainTeamA())
            .closed(match.isClosed())
            .build());
  }

  @Override
  public void delete(long id) {
    matchRepository.deleteById(id);
  }

  @Override
  public void closePastMatches() {
    matchRepository
        .findByClosedAndMatchDayBefore(false, LocalDate.now())
        .forEach(
            matchEntity -> {
              try {
                close(matchEntity.getId());
              } catch (MatchNotFoundException e) {
                throw new RuntimeException(e);
              }
            });
  }

  private MatchPlayerDto getPlayer(long playerId) {
    var member = memberRepository.findById(playerId);
    return new MatchPlayerDto(
        playerId,
        member
            .map(memberEntity -> memberEntity.getName() + " " + memberEntity.getSurname())
            .orElse("Not found"),
        member.map(MemberEntity::getAlias).orElse(null));
  }

  private MatchDto matchToDto(MatchEntity entity) {
    return new MatchDto(
        entity.getId(),
        formatter.format(entity.getMatchDay()),
        entity.getConfirmedPlayers().stream().map(this::getPlayer).collect(Collectors.toSet()),
        entity.getUnConfirmedPlayers().stream().map(this::getPlayer).collect(Collectors.toSet()),
        entity.getNotAvailablePlayers().stream().map(this::getPlayer).collect(Collectors.toSet()),
        entity.getTeamAPlayers().stream().map(this::getPlayer).toList(),
        entity.getTeamBPlayers().stream().map(this::getPlayer).toList(),
        entity.getTeamAGuests(),
        entity.getTeamBGuests(),
        entity.getCaptainTeamA() != null ? getPlayer(entity.getCaptainTeamA()) : null,
        entity.getCaptainTeamB() != null ? getPlayer(entity.getCaptainTeamB()) : null,
        entity.isClosed());
  }
}
