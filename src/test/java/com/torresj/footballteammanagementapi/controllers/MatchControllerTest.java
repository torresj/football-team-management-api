package com.torresj.footballteammanagementapi.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.footballteammanagementapi.dtos.*;
import com.torresj.footballteammanagementapi.entities.MatchEntity;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.enums.Role;
import com.torresj.footballteammanagementapi.exceptions.MatchNotFoundException;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.repositories.MatchRepository;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.repositories.MovementRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

        matchRepository.delete(matchEntity);
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

        matchRepository.deleteAll(matches);
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

        matchRepository.deleteAll(matches);
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

        matchRepository.delete(matchRepository.findByMatchDay(match.matchDay()).get());
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

        matchRepository.delete(matchInDb);
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

        for(long player: match.getNotAvailablePlayers()){
            Assertions.assertFalse(movementRepository.findByMemberId(player).isEmpty());
        }

        movementRepository.deleteAll();
        matchRepository.delete(matchClosed);
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
}
