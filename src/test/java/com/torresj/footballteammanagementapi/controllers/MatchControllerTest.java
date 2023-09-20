package com.torresj.footballteammanagementapi.controllers;

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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
                                                .map(MemberEntity::getId).collect(Collectors.toSet()))
                                .teamAPlayers(new ArrayList<>())
                                .teamBPlayers(new ArrayList<>())
                                .teamAGuests(new ArrayList<>())
                                .teamBGuests(new ArrayList<>())
                                .closed(false)
                                .build(), MatchEntity.builder()
                                .matchDay(LocalDate.now().minusDays(1))
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

        if (token == null) loginWithUser("MatchUser1");

        var result =
                mockMvc
                        .perform(get("/v1/matches").header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk());

        var content = result.andReturn().getResponse().getContentAsString();
        List<MatchDto> matches =
                objectMapper.readValue(content, new TypeReference<>() {
                });

        Assertions.assertEquals(2, matches.size());
        matchRepository.deleteAll();
    }

    @Test
    @DisplayName("Get match by ID")
    void getMatchById() throws Exception {
        var matchEntity =
                matchRepository.save(MatchEntity.builder()
                        .matchDay(LocalDate.now())
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
                        .build());

        if (token == null) loginWithUser("MatchUser2");

        mockMvc
                .perform(
                        get("/v1/matches/" + matchEntity.getId())
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        matchRepository.deleteAll();
    }

    @Test
    @DisplayName("Get match by ID that doesn't exist")
    void getMatchByIdNotExist() throws Exception {
        if (token == null) loginWithUser("MatchUser3");

        mockMvc
                .perform(
                        get("/v1/matches/1234")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get next match")
    void getNextMatch() throws Exception {
        var matches =
                matchRepository.saveAll(List.of(MatchEntity.builder()
                        .matchDay(LocalDate.now())
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
                        .build(), MatchEntity.builder()
                        .matchDay(LocalDate.now().minusDays(7))
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

        if (token == null) loginWithUser("MatchUser4");

        var result = mockMvc
                .perform(
                        get("/v1/matches/next")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        var content = result.andReturn().getResponse().getContentAsString();
        var match = objectMapper.readValue(content, MatchDto.class);

        Assertions.assertEquals(matches.get(0).getMatchDay(), match.matchDay());

        matchRepository.deleteAll();
    }

    @Test
    @DisplayName("Get next match not created yet")
    void getNextMatchNotCreatedYet() throws Exception {
        var matches =
                matchRepository.saveAll(List.of(MatchEntity.builder()
                        .matchDay(LocalDate.now().minusDays(7))
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
                        .build(), MatchEntity.builder()
                        .matchDay(LocalDate.now().minusDays(14))
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

        if (token == null) loginWithUser("MatchUser5");

        mockMvc
                .perform(
                        get("/v1/matches/next")
                                .header("Authorization", "Bearer " + token))
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
        var matchInDb = matchRepository.save(MatchEntity.builder()
                .matchDay(LocalDate.now())
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

        players.add(1234L);
        players.add(1235L);

        var match =
                matchRepository.save(MatchEntity.builder()
                        .matchDay(LocalDate.now().minusDays(7))
                        .confirmedPlayers(new HashSet<>())
                        .notAvailablePlayers(players)
                        .unConfirmedPlayers(
                                memberRepository.findAll().stream()
                                        .filter(memberEntity -> adminUser.equals(memberEntity.getName()))
                                        .map(MemberEntity::getId).collect(Collectors.toSet()))
                        .teamAPlayers(new ArrayList<>())
                        .teamBPlayers(new ArrayList<>())
                        .teamAGuests(new ArrayList<>())
                        .teamBGuests(new ArrayList<>())
                        .closed(false)
                        .build());

        if (adminToken == null) loginWithAdmin();

        mockMvc
                .perform(
                        post("/v1/matches/" + match.getId() + "/close")
                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        var matchClosed = matchRepository
                .findById(match.getId()).orElseThrow(() -> new MatchNotFoundException(match.getId()));

        Assertions.assertTrue(matchClosed.isClosed());

        for (long player : match.getNotAvailablePlayers()) {
            Assertions.assertFalse(movementRepository.findByMemberId(player).isEmpty());
        }

        movementRepository.deleteAll();
        matchRepository.deleteAll();
    }

    @Test
    @DisplayName("Close match that doesn't exist")
    void closeMatchNotExists() throws Exception {

        if (adminToken == null) loginWithAdmin();

        mockMvc
                .perform(
                        post("/v1/matches/1234/close")
                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Close match without admin role")
    void closeMatchNoAdminRole() throws Exception {

        if (token == null) loginWithUser("MatchUser7");

        mockMvc
                .perform(
                        post("/v1/matches/1234/close")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Add player available")
    void addPlayer() throws Exception {

        if (token == null) loginWithUser("MatchUser8");

        var match =
                matchRepository.save(MatchEntity.builder()
                        .matchDay(LocalDate.now().plusDays(7))
                        .confirmedPlayers(new HashSet<>())
                        .notAvailablePlayers(new HashSet<>())
                        .unConfirmedPlayers(
                                memberRepository.findAll().stream()
                                        .filter(memberEntity -> !adminUser.equals(memberEntity.getName()))
                                        .map(MemberEntity::getId).collect(Collectors.toSet()))
                        .teamAPlayers(new ArrayList<>())
                        .teamBPlayers(new ArrayList<>())
                        .teamAGuests(new ArrayList<>())
                        .teamBGuests(new ArrayList<>())
                        .closed(false)
                        .build());

        var request = new AddPlayerRequest(PlayerMatchStatus.AVAILABLE);

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
        Assertions.assertFalse(matchFromDB.get().getUnConfirmedPlayers().contains(player.get().getId()));

        matchRepository.deleteAll();
    }

    @Test
    @DisplayName("Add player not available")
    void addPlayerNotAvailable() throws Exception {

        if (token == null) loginWithUser("MatchUser9");

        var match =
                matchRepository.save(MatchEntity.builder()
                        .matchDay(LocalDate.now().plusDays(7))
                        .confirmedPlayers(new HashSet<>())
                        .notAvailablePlayers(new HashSet<>())
                        .unConfirmedPlayers(
                                memberRepository.findAll().stream()
                                        .filter(memberEntity -> !adminUser.equals(memberEntity.getName()))
                                        .map(MemberEntity::getId).collect(Collectors.toSet()))
                        .teamAPlayers(new ArrayList<>())
                        .teamBPlayers(new ArrayList<>())
                        .teamAGuests(new ArrayList<>())
                        .teamBGuests(new ArrayList<>())
                        .closed(false)
                        .build());

        var request = new AddPlayerRequest(PlayerMatchStatus.NOT_AVAILABLE);

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

        Assertions.assertTrue(matchFromDB.get().getNotAvailablePlayers().contains(player.get().getId()));
        Assertions.assertFalse(matchFromDB.get().getUnConfirmedPlayers().contains(player.get().getId()));

        matchRepository.deleteAll();
    }

    @Test
    @DisplayName("Add player match not exists")
    void addPlayerMatchNotExists() throws Exception {

        if (token == null) loginWithUser("MatchUser10");

        var request = new AddPlayerRequest(PlayerMatchStatus.NOT_AVAILABLE);

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

        var player = memberRepository.save(MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

        var players = new HashSet<Long>();
        players.add(player.getId());

        var match =
                matchRepository.save(MatchEntity.builder()
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

        var player = memberRepository.save(MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

        var players = new HashSet<Long>();
        players.add(player.getId());

        var match =
                matchRepository.save(MatchEntity.builder()
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
                matchRepository.save(MatchEntity.builder()
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

        var player = memberRepository.save(MemberEntity.builder()
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

        var player = memberRepository.save(MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

        var players = new HashSet<Long>();
        players.add(player.getId());

        var match =
                matchRepository.save(MatchEntity.builder()
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

        var player = memberRepository.save(MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

        var players = new HashSet<Long>();
        players.add(player.getId());

        var match =
                matchRepository.save(MatchEntity.builder()
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
                matchRepository.save(MatchEntity.builder()
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

        var player = memberRepository.save(MemberEntity.builder()
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

        var player = memberRepository.save(MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

        var players = new ArrayList<Long>();
        players.add(player.getId());

        var match =
                matchRepository.save(MatchEntity.builder()
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

        var player = memberRepository.save(MemberEntity.builder()
                .name("player")
                .surname("test")
                .password("test")
                .phone("")
                .role(Role.USER)
                .build());

        var players = new ArrayList<Long>();
        players.add(player.getId());

        var match =
                matchRepository.save(MatchEntity.builder()
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
}
