package com.torresj.footballteammanagementapi.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.footballteammanagementapi.dtos.MemberDto;
import com.torresj.footballteammanagementapi.dtos.MovementDto;
import com.torresj.footballteammanagementapi.dtos.RequestLoginDto;
import com.torresj.footballteammanagementapi.dtos.ResponseLoginDto;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.entities.MovementEntity;
import com.torresj.footballteammanagementapi.enums.MovementType;
import com.torresj.footballteammanagementapi.enums.Role;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.repositories.MovementRepository;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerTest {
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private MockMvc mockMvc;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired private MemberRepository memberRepository;
  @Autowired private MovementRepository movementRepository;

  @Value("${admin.user}")
  private String adminUser;

  @Value("${admin.password}")
  private String adminPassword;

  private String token;

  void loginWithAdmin() throws Exception {
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
    token = response.jwt();
  }

  @Test
  @DisplayName("Get all members")
  void getAllMembers() throws Exception {
    var membersEntities =
        memberRepository.saveAll(
            List.of(
                MemberEntity.builder()
                    .name("test1")
                    .surname("test1")
                    .password("test1")
                    .phone("")
                    .role(Role.USER)
                    .build(),
                MemberEntity.builder()
                    .name("test2")
                    .surname("test2")
                    .password("test2")
                    .phone("")
                    .role(Role.USER)
                    .build()));

    if (token == null) loginWithAdmin();

    var result =
        mockMvc
            .perform(get("/v1/members").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

    var content = result.andReturn().getResponse().getContentAsString();
    List<MemberDto> members =
        objectMapper.readValue(content, new TypeReference<List<MemberDto>>() {});

    Assertions.assertEquals(2, members.size());
    memberRepository.deleteAll(membersEntities);
  }

  @Test
  @DisplayName("Get member by ID")
  void getMemberById() throws Exception {
    var memberEntity =
        memberRepository.save(
            MemberEntity.builder()
                .name("test1")
                .surname("test1")
                .password("test1")
                .phone("")
                .role(Role.USER)
                .build());

    if (token == null) loginWithAdmin();

    mockMvc
        .perform(
            get("/v1/members/" + memberEntity.getId()).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    memberRepository.delete(memberEntity);
  }

  @Test
  @DisplayName("Get member by ID that doesn't exist")
  void getMemberByIdNotExists() throws Exception {
    if (token == null) loginWithAdmin();

    mockMvc
        .perform(
            get("/v1/members/" + new Random().nextInt()).header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Get member movements")
  void getMemberMovements() throws Exception {
    var memberEntity =
        memberRepository.save(
            MemberEntity.builder()
                .name("test1")
                .surname("test1")
                .password("test1")
                .phone("")
                .role(Role.USER)
                .build());

    movementRepository.saveAll(
        List.of(
            MovementEntity.builder()
                .memberId(memberEntity.getId())
                .type(MovementType.EXPENSE)
                .description("")
                .amount(-30)
                .build(),
            MovementEntity.builder()
                .memberId(memberEntity.getId())
                .type(MovementType.INCOME)
                .description("")
                .amount(20)
                .build()));

    if (token == null) loginWithAdmin();

    var movementResults =
        mockMvc
            .perform(
                get("/v1/members/" + memberEntity.getId() + "/movements")
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

    var memberResult =
        mockMvc
            .perform(
                get("/v1/members/" + memberEntity.getId())
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

    var movementContent = movementResults.andReturn().getResponse().getContentAsString();
    List<MovementDto> movements =
        objectMapper.readValue(movementContent, new TypeReference<List<MovementDto>>() {});

    var memberContent = memberResult.andReturn().getResponse().getContentAsString();
    MemberDto member = objectMapper.readValue(memberContent, MemberDto.class);

    Assertions.assertEquals(2, movements.size());
    Assertions.assertEquals(-10, member.balance());

    memberRepository.delete(memberEntity);
    movementRepository.deleteAll();
  }

  @Test
  @DisplayName("Get member movements from member that doesn't exist")
  void getMemberMovementsNotExists() throws Exception {

    movementRepository.saveAll(
        List.of(
            MovementEntity.builder()
                .memberId(10)
                .type(MovementType.EXPENSE)
                .description("")
                .amount(-30)
                .build(),
            MovementEntity.builder()
                .memberId(10)
                .type(MovementType.INCOME)
                .description("")
                .amount(20)
                .build()));

    if (token == null) loginWithAdmin();

    mockMvc
        .perform(get("/v1/members/10/movements").header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());

    movementRepository.deleteAll();
  }
}
