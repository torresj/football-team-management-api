package com.torresj.footballteammanagementapi.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.footballteammanagementapi.dtos.*;
import com.torresj.footballteammanagementapi.entities.MatchEntity;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.enums.PlayerMatchStatus;
import com.torresj.footballteammanagementapi.enums.Role;
import com.torresj.footballteammanagementapi.exceptions.MatchNotFoundException;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.repositories.MatchRepository;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.repositories.MovementRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class MatchControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private MemberRepository memberRepository;

  @Autowired private MatchRepository matchRepository;

  @Autowired private MovementRepository movementRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Value("${admin.user}")
  private String adminUser;

  @Value("${admin.password}")
  private String adminPassword;

  private String adminToken;
  private String token;

  private void loginWithAdmin() throws Exception {
    var member =
        memberRepository
            .findByNameAndSurname(adminUser, adminUser)
            .orElseThrow(() -> new MemberNotFoundException(""));

    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new RequestLoginDto(
                                adminUser + "." + adminUser,
                                adminPassword,
                                member.getNonce() + 1))))
            .andExpect(status().isOk());
    var content = result.andReturn().getResponse().getContentAsString();
    ResponseLoginDto response = objectMapper.readValue(content, ResponseLoginDto.class);
    adminToken = response.jwt();
  }

  private void loginWithUser(String name) throws Exception {
    var entity =
        memberRepository
            .findByNameAndSurname(name, name)
            .orElse(
                memberRepository.save(
                    MemberEntity.builder()
                        .role(Role.USER)
                        .phone("")
                        .password(passwordEncoder.encode("test"))
                        .name(name)
                        .surname(name)
                        .build()));

    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new RequestLoginDto(
                                entity.getName() + "." + entity.getSurname(),
                                "test",
                                entity.getNonce() + 1))))
            .andExpect(status().isOk());
    var content = result.andReturn().getResponse().getContentAsString();
    ResponseLoginDto response = objectMapper.readValue(content, ResponseLoginDto.class);
    token = response.jwt();
  }

  @Test
  @DisplayName("Get all matches")
  void getAllMatches() throws Exception {
    matchRepository.saveAll(
        List.of(
            MatchEntity.builder()
                .matchDay(LocalDate.now().minusDays(2))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(
                    memberRepository.findAll().stream()
                        .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                        .map(MemberEntity::getId)
                        .collect(Collectors.toSet()))
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(true)
                .build(),
            MatchEntity.builder()
                .matchDay(LocalDate.now().minusDays(1))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(
                    memberRepository.findAll().stream()
                        .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                        .map(MemberEntity::getId)
                        .collect(Collectors.toSet()))
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(true)
                .build(),
            MatchEntity.builder()
                .matchDay(LocalDate.now().minusDays(10))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(
                    memberRepository.findAll().stream()
                        .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                        .map(MemberEntity::getId)
                        .collect(Collectors.toSet()))
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build()));

    if (token == null) loginWithUser("MatchUser1");

    var result =
        mockMvc
            .perform(get("/v1/matches").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

    var content = result.andReturn().getResponse().getContentAsString();
    List<MatchDto> matches = objectMapper.readValue(content, new TypeReference<>() {});

    Assertions.assertEquals(2, matches.size());
    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Get match by ID")
  void getMatchById() throws Exception {
    var matchEntity =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now())
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(
                    memberRepository.findAll().stream()
                        .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                        .map(MemberEntity::getId)
                        .collect(Collectors.toSet()))
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    if (token == null) loginWithUser("MatchUser2");

    mockMvc
        .perform(
            get("/v1/matches/" + matchEntity.getId()).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Get match by ID that doesn't exist")
  void getMatchByIdNotExist() throws Exception {
    if (token == null) loginWithUser("MatchUser3");

    mockMvc
        .perform(get("/v1/matches/1234").header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Get next match")
  void getNextMatch() throws Exception {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    var matches =
        matchRepository.saveAll(
            List.of(
                MatchEntity.builder()
                    .matchDay(LocalDate.now())
                    .confirmedPlayers(new HashSet<>())
                    .notAvailablePlayers(new HashSet<>())
                    .unConfirmedPlayers(
                        memberRepository.findAll().stream()
                            .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                            .map(MemberEntity::getId)
                            .collect(Collectors.toSet()))
                    .teamAPlayers(new ArrayList<>())
                    .teamBPlayers(new ArrayList<>())
                    .teamAGuests(new ArrayList<>())
                    .teamBGuests(new ArrayList<>())
                    .closed(false)
                    .build(),
                MatchEntity.builder()
                    .matchDay(LocalDate.now().minusDays(7))
                    .confirmedPlayers(new HashSet<>())
                    .notAvailablePlayers(new HashSet<>())
                    .unConfirmedPlayers(
                        memberRepository.findAll().stream()
                            .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                            .map(MemberEntity::getId)
                            .collect(Collectors.toSet()))
                    .teamAPlayers(new ArrayList<>())
                    .teamBPlayers(new ArrayList<>())
                    .teamAGuests(new ArrayList<>())
                    .teamBGuests(new ArrayList<>())
                    .closed(false)
                    .build()));

    if (token == null) loginWithUser("MatchUser4");

    var result =
        mockMvc
            .perform(get("/v1/matches/next").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

    var content = result.andReturn().getResponse().getContentAsString();
    var match = objectMapper.readValue(content, MatchDto.class);

    Assertions.assertEquals(formatter.format(matches.get(0).getMatchDay()), match.matchDay());

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Get next match not created yet")
  void getNextMatchNotCreatedYet() throws Exception {
    matchRepository.saveAll(
        List.of(
            MatchEntity.builder()
                .matchDay(LocalDate.now().minusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(
                    memberRepository.findAll().stream()
                        .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                        .map(MemberEntity::getId)
                        .collect(Collectors.toSet()))
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build(),
            MatchEntity.builder()
                .matchDay(LocalDate.now().minusDays(14))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(
                    memberRepository.findAll().stream()
                        .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                        .map(MemberEntity::getId)
                        .collect(Collectors.toSet()))
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build()));

    if (token == null) loginWithUser("MatchUser5");

    mockMvc
        .perform(get("/v1/matches/next").header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Create match")
  void createMatch() throws Exception {
    var match = new CreateMatchDto(LocalDate.now().plusDays(1));

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            post("/v1/matches")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(match)))
        .andExpect(status().isCreated());

    Assertions.assertTrue(matchRepository.findByMatchDay(match.matchDay()).isPresent());

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Create match with an existing match already created")
  void createMatchAlreadyCreated() throws Exception {
    matchRepository.save(
        MatchEntity.builder()
            .matchDay(LocalDate.now())
            .confirmedPlayers(new HashSet<>())
            .notAvailablePlayers(new HashSet<>())
            .unConfirmedPlayers(
                memberRepository.findAll().stream()
                    .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                    .map(MemberEntity::getId)
                    .collect(Collectors.toSet()))
            .teamAPlayers(new ArrayList<>())
            .teamBPlayers(new ArrayList<>())
            .teamAGuests(new ArrayList<>())
            .teamBGuests(new ArrayList<>())
            .closed(false)
            .build());

    var match = new CreateMatchDto(LocalDate.now().plusDays(1));

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            post("/v1/matches")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(match)))
        .andExpect(status().isBadRequest());

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Create match without admin role")
  void createMatchNotAdminRole() throws Exception {
    var match = new CreateMatchDto(LocalDate.now().plusDays(1));

    if (token == null) loginWithUser("MatchUser6");

    mockMvc
        .perform(
            post("/v1/matches")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(match)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Close match")
  void closeMatch() throws Exception {

    var players = new HashSet<Long>();

    var members =
        memberRepository.saveAll(
            List.of(
                MemberEntity.builder()
                    .role(Role.USER)
                    .phone("")
                    .password("test")
                    .name("test")
                    .surname("test1")
                    .injured(false)
                    .build(),
                MemberEntity.builder()
                    .role(Role.USER)
                    .phone("")
                    .password("test")
                    .name("test")
                    .surname("test2")
                    .injured(false)
                    .build(),
                MemberEntity.builder()
                    .role(Role.USER)
                    .phone("")
                    .password("test")
                    .name("test")
                    .surname("test3")
                    .injured(true)
                    .build()));

    players.add(members.get(0).getId());
    players.add(members.get(1).getId());
    players.add(members.get(2).getId());

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().minusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(players)
                .unConfirmedPlayers(
                    memberRepository.findAll().stream()
                        .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                        .map(MemberEntity::getId)
                        .collect(Collectors.toSet()))
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .captainTeamA(null)
                .captainTeamB(null)
                .closed(false)
                .build());

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/close")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk());

    var matchClosed =
        matchRepository
            .findById(match.getId())
            .orElseThrow(() -> new MatchNotFoundException(match.getId()));

    Assertions.assertTrue(matchClosed.isClosed());

    Assertions.assertFalse(
        movementRepository
            .findByMemberId(members.get(0).getId(), Sort.by(Sort.Direction.DESC, "createdOn"))
            .isEmpty());
    Assertions.assertFalse(
        movementRepository
            .findByMemberId(members.get(1).getId(), Sort.by(Sort.Direction.DESC, "createdOn"))
            .isEmpty());
    Assertions.assertTrue(
        movementRepository
            .findByMemberId(members.get(2).getId(), Sort.by(Sort.Direction.DESC, "createdOn"))
            .isEmpty());

    movementRepository.deleteAll();
    matchRepository.deleteAll();
    memberRepository.deleteAll(members);
  }

  @Test
  @DisplayName("Close match with Captains")
  void closeMatchWithCaptains() throws Exception {

    var players = new HashSet<Long>();

    var members =
        memberRepository.saveAll(
            List.of(
                MemberEntity.builder()
                    .role(Role.USER)
                    .phone("")
                    .password("test")
                    .name("test")
                    .surname("test1")
                    .injured(false)
                    .build(),
                MemberEntity.builder()
                    .role(Role.USER)
                    .phone("")
                    .password("test")
                    .name("test")
                    .surname("test2")
                    .injured(false)
                    .build(),
                MemberEntity.builder()
                    .role(Role.USER)
                    .phone("")
                    .password("test")
                    .name("test")
                    .surname("test3")
                    .injured(true)
                    .build()));

    players.add(members.get(0).getId());
    players.add(members.get(1).getId());
    players.add(members.get(2).getId());

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().minusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(players)
                .unConfirmedPlayers(
                    memberRepository.findAll().stream()
                        .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                        .map(MemberEntity::getId)
                        .collect(Collectors.toSet()))
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .captainTeamA(members.get(0).getId())
                .captainTeamB(members.get(1).getId())
                .closed(false)
                .build());

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/close")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk());

    var matchClosed =
        matchRepository
            .findById(match.getId())
            .orElseThrow(() -> new MatchNotFoundException(match.getId()));

    Assertions.assertTrue(matchClosed.isClosed());

    Assertions.assertFalse(
        movementRepository
            .findByMemberId(members.get(0).getId(), Sort.by(Sort.Direction.DESC, "createdOn"))
            .isEmpty());
    Assertions.assertFalse(
        movementRepository
            .findByMemberId(members.get(1).getId(), Sort.by(Sort.Direction.DESC, "createdOn"))
            .isEmpty());
    Assertions.assertTrue(
        movementRepository
            .findByMemberId(members.get(2).getId(), Sort.by(Sort.Direction.DESC, "createdOn"))
            .isEmpty());
    Assertions.assertEquals(
        1, memberRepository.findById(members.get(0).getId()).get().getNCaptaincies());
    Assertions.assertEquals(
        1, memberRepository.findById(members.get(1).getId()).get().getNCaptaincies());

    movementRepository.deleteAll();
    matchRepository.deleteAll();
    memberRepository.deleteAll(members);
  }

  @Test
  @DisplayName("Close match that doesn't exist")
  void closeMatchNotExists() throws Exception {

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(post("/v1/matches/1234/close").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Close match without admin role")
  void closeMatchNoAdminRole() throws Exception {

    if (token == null) loginWithUser("MatchUser7");

    mockMvc
        .perform(post("/v1/matches/1234/close").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Add player available")
  void addPlayer() throws Exception {

    if (token == null) loginWithUser("MatchUser8");

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
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
                .build());

    var request = new AddPlayerRequestDto(PlayerMatchStatus.AVAILABLE);

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/players")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());
    var player = memberRepository.findByNameAndSurname("MatchUser8", "MatchUser8");

    Assertions.assertTrue(matchFromDB.get().getConfirmedPlayers().contains(player.get().getId()));
    Assertions.assertFalse(
        matchFromDB.get().getUnConfirmedPlayers().contains(player.get().getId()));

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Add player not available")
  void addPlayerNotAvailable() throws Exception {

    if (token == null) loginWithUser("MatchUser9");

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
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
                .build());

    var request = new AddPlayerRequestDto(PlayerMatchStatus.NOT_AVAILABLE);

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/players")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());
    var player = memberRepository.findByNameAndSurname("MatchUser9", "MatchUser9");

    Assertions.assertTrue(
        matchFromDB.get().getNotAvailablePlayers().contains(player.get().getId()));
    Assertions.assertFalse(
        matchFromDB.get().getUnConfirmedPlayers().contains(player.get().getId()));

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Add player blocked")
  void addPlayerBlocked() throws Exception {

    var user = "MatchUser50";

    if (token == null) loginWithUser(user);

    var member =
        memberRepository
            .findByNameAndSurname(user, user)
            .orElseThrow(() -> new MemberNotFoundException(user));

    memberRepository.save(
        MemberEntity.builder()
            .id(member.getId())
            .name(member.getName())
            .alias(member.getAlias())
            .surname(member.getSurname())
            .phone(member.getPhone())
            .role(member.getRole())
            .nCaptaincies(member.getNCaptaincies())
            .nonce(member.getNonce())
            .password(member.getPassword())
            .injured(member.isInjured())
            .blocked(true)
            .build());

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
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
                .build());

    var request = new AddPlayerRequestDto(PlayerMatchStatus.AVAILABLE);

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/players")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Add player match not exists")
  void addPlayerMatchNotExists() throws Exception {

    if (token == null) loginWithUser("MatchUser10");

    var request = new AddPlayerRequestDto(PlayerMatchStatus.NOT_AVAILABLE);

    mockMvc
        .perform(
            post("/v1/matches/1234/players")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());

    var player = memberRepository.findByNameAndSurname("MatchUser10", "MatchUser10");
    memberRepository.delete(player.get());
  }

  @Test
  @DisplayName("Add player to team A")
  void addPlayerTeamA() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var player =
        memberRepository.save(
            MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

    var players = new HashSet<Long>();
    players.add(player.getId());

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(players)
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/players/" + player.getId() + "/teama")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());

    Assertions.assertTrue(matchFromDB.get().getTeamAPlayers().contains(player.getId()));

    matchRepository.deleteAll();
    memberRepository.delete(player);
  }

  @Test
  @DisplayName("Add player to team A not available")
  void addPlayerTeamANotAvailable() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var player =
        memberRepository.save(
            MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

    var players = new HashSet<Long>();
    players.add(player.getId());

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(players)
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/players/" + player.getId() + "/teama")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    matchRepository.deleteAll();
    memberRepository.delete(player);
  }

  @Test
  @DisplayName("Add player to team A player doesn't exist")
  void addPlayerTeamAPlayerNotExist() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var players = new HashSet<Long>();

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(players)
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/players/1234/teama")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Add player to team A match doesn't exist")
  void addPlayerTeamAMatchNotExist() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var player =
        memberRepository.save(
            MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

    mockMvc
        .perform(
            post("/v1/matches/1234/players/" + player.getId() + "/teama")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    memberRepository.delete(player);
  }

  @Test
  @DisplayName("Add player to team A without admin role")
  void addPlayerTeamANoAdminRole() throws Exception {

    if (token == null) loginWithUser("MatchUser11");

    mockMvc
        .perform(
            post("/v1/matches/1234/players/1234/teama")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Add player to team B")
  void addPlayerTeamB() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var player =
        memberRepository.save(
            MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

    var players = new HashSet<Long>();
    players.add(player.getId());

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(players)
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/players/" + player.getId() + "/teamb")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());

    Assertions.assertTrue(matchFromDB.get().getTeamBPlayers().contains(player.getId()));

    matchRepository.deleteAll();
    memberRepository.delete(player);
  }

  @Test
  @DisplayName("Add player to team B not available")
  void addPlayerTeamBNotAvailable() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var player =
        memberRepository.save(
            MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

    var players = new HashSet<Long>();
    players.add(player.getId());

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(players)
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/players/" + player.getId() + "/teamb")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    matchRepository.deleteAll();
    memberRepository.delete(player);
  }

  @Test
  @DisplayName("Add player to team B player doesn't exist")
  void addPlayerTeamBPlayerNotExist() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var players = new HashSet<Long>();

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(players)
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/players/1234/teamb")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Add player to team B match doesn't exist")
  void addPlayerTeamBMatchNotExist() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var player =
        memberRepository.save(
            MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

    mockMvc
        .perform(
            post("/v1/matches/1234/players/" + player.getId() + "/teama")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    memberRepository.delete(player);
  }

  @Test
  @DisplayName("Add player to team B without admin role")
  void addPlayerTeamBNoAdminRole() throws Exception {

    if (token == null) loginWithUser("MatchUser12");

    mockMvc
        .perform(
            post("/v1/matches/1234/players/1234/teamb")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Remove player from team A")
  void removePlayerFromTeamA() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var player =
        memberRepository.save(
            MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

    var players = new ArrayList<Long>();
    players.add(player.getId());

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(players)
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    mockMvc
        .perform(
            delete("/v1/matches/" + match.getId() + "/players/" + player.getId() + "/teama")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());

    Assertions.assertFalse(matchFromDB.get().getTeamAPlayers().contains(player.getId()));

    matchRepository.deleteAll();
    memberRepository.delete(player);
  }

  @Test
  @DisplayName("Remove player from team B")
  void removePlayerFromTeamB() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var player =
        memberRepository.save(
            MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

    var players = new ArrayList<Long>();
    players.add(player.getId());

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(players)
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    mockMvc
        .perform(
            delete("/v1/matches/" + match.getId() + "/players/" + player.getId() + "/teamb")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());

    Assertions.assertFalse(matchFromDB.get().getTeamBPlayers().contains(player.getId()));

    matchRepository.deleteAll();
    memberRepository.delete(player);
  }

  @Test
  @DisplayName("Add guest to team A")
  void addGuestTeamA() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    var request = new GuestRequestDto("guest");

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/guests/teama")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());

    Assertions.assertTrue(matchFromDB.get().getTeamAGuests().contains(request.guest()));

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Add guest to team B")
  void addGuestTeamB() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    var request = new GuestRequestDto("guest");

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/guests/teamb")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());

    Assertions.assertTrue(matchFromDB.get().getTeamBGuests().contains(request.guest()));

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Delete guest from team A")
  void deleteGuestFromTeamA() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(List.of("guest"))
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    var request = new GuestRequestDto("guest");

    mockMvc
        .perform(
            delete("/v1/matches/" + match.getId() + "/guests/teama")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());

    Assertions.assertTrue(matchFromDB.get().getTeamAGuests().isEmpty());

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Delete guest from team B")
  void deleteGuestFromTeamB() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(new HashSet<>())
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(List.of("guest"))
                .closed(false)
                .build());

    var request = new GuestRequestDto("guest");

    mockMvc
        .perform(
            delete("/v1/matches/" + match.getId() + "/guests/teamb")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());

    Assertions.assertTrue(matchFromDB.get().getTeamBGuests().isEmpty());

    matchRepository.deleteAll();
  }

  @Test
  @DisplayName("Add guest to team A without admin role")
  void addGuestTeamANoAdminRole() throws Exception {

    if (token == null) loginWithUser("MatchUser13");

    var request = new GuestRequestDto("guest");

    mockMvc
        .perform(
            post("/v1/matches/1234/guests/teama")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Add guest to team B without admin role")
  void addGuestTeamBNoAdminRole() throws Exception {

    if (token == null) loginWithUser("MatchUser14");

    var request = new GuestRequestDto("guest");

    mockMvc
        .perform(
            post("/v1/matches/1234/guests/teamb")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Remove guest to team A without admin role")
  void removeGuestTeamANoAdminRole() throws Exception {

    if (token == null) loginWithUser("MatchUser15");

    var request = new GuestRequestDto("guest");

    mockMvc
        .perform(
            delete("/v1/matches/1234/guests/teama")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Remove guest to team B without admin role")
  void removeGuestTeamBNoAdminRole() throws Exception {

    if (token == null) loginWithUser("MatchUser16");

    var request = new GuestRequestDto("guest");

    mockMvc
        .perform(
            delete("/v1/matches/1234/guests/teamb")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Add player to team A as captain")
  void addPlayerTeamAAsCaptain() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var members =
        memberRepository.saveAll(
            List.of(
                MemberEntity.builder()
                    .name("player")
                    .surname("test")
                    .password("test")
                    .phone("")
                    .role(Role.USER)
                    .nCaptaincies(0)
                    .build(),
                MemberEntity.builder()
                    .name("player1")
                    .surname("test")
                    .password("test")
                    .phone("")
                    .role(Role.USER)
                    .nCaptaincies(0)
                    .build(),
                MemberEntity.builder()
                    .name("player2")
                    .surname("test")
                    .password("test")
                    .phone("")
                    .role(Role.USER)
                    .nCaptaincies(2)
                    .build()));

    var players = new HashSet<Long>();
    players.add(members.get(0).getId());
    players.add(members.get(1).getId());
    players.add(members.get(2).getId());

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(players)
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(players.stream().toList())
                .teamBPlayers(new ArrayList<>())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/captainA")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());

    Assertions.assertNotEquals(matchFromDB.get().getCaptainTeamA(), members.get(2).getId());

    matchRepository.deleteAll();
    memberRepository.deleteAll(members);
  }

  @Test
  @DisplayName("Add player to team B as captain")
  void addPlayerTeamBAsCaptain() throws Exception {

    if (adminToken == null) loginWithAdmin();

    var members =
        memberRepository.saveAll(
            List.of(
                MemberEntity.builder()
                    .name("player")
                    .surname("test")
                    .password("test")
                    .phone("")
                    .role(Role.USER)
                    .nCaptaincies(0)
                    .build(),
                MemberEntity.builder()
                    .name("player1")
                    .surname("test")
                    .password("test")
                    .phone("")
                    .role(Role.USER)
                    .nCaptaincies(0)
                    .build(),
                MemberEntity.builder()
                    .name("player2")
                    .surname("test")
                    .password("test")
                    .phone("")
                    .role(Role.USER)
                    .nCaptaincies(2)
                    .build()));

    var players = new HashSet<Long>();
    players.add(members.get(0).getId());
    players.add(members.get(1).getId());
    players.add(members.get(2).getId());

    var match =
        matchRepository.save(
            MatchEntity.builder()
                .matchDay(LocalDate.now().plusDays(7))
                .confirmedPlayers(players)
                .notAvailablePlayers(new HashSet<>())
                .unConfirmedPlayers(new HashSet<>())
                .teamAPlayers(new ArrayList<>())
                .teamBPlayers(players.stream().toList())
                .teamAGuests(new ArrayList<>())
                .teamBGuests(new ArrayList<>())
                .closed(false)
                .build());

    mockMvc
        .perform(
            post("/v1/matches/" + match.getId() + "/captainB")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    var matchFromDB = matchRepository.findById(match.getId());

    Assertions.assertNotEquals(matchFromDB.get().getCaptainTeamB(), members.get(2).getId());

    matchRepository.deleteAll();
    memberRepository.deleteAll(members);
  }
}
