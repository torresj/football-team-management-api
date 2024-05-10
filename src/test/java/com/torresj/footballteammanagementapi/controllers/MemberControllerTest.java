package com.torresj.footballteammanagementapi.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.footballteammanagementapi.dtos.*;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.entities.MovementEntity;
import com.torresj.footballteammanagementapi.enums.MovementType;
import com.torresj.footballteammanagementapi.enums.Role;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.repositories.MovementRepository;

import java.util.List;
import java.util.Random;

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

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private MemberRepository memberRepository;
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

    if (adminToken == null) loginWithAdmin();

    var result =
        mockMvc
            .perform(get("/v1/members").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());

    var content = result.andReturn().getResponse().getContentAsString();
    List<MemberDto> members = objectMapper.readValue(content, new TypeReference<>() {});

    Assertions.assertTrue(members.size() >= 2);
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

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            get("/v1/members/" + memberEntity.getId())
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk());

    memberRepository.delete(memberEntity);
  }

  @Test
  @DisplayName("Get logged member")
  void getMe() throws Exception {

    if (token == null) loginWithUser("userLogged");

    var result =
        mockMvc
            .perform(get("/v1/members/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

    var content = result.andReturn().getResponse().getContentAsString();
    MemberDto response = objectMapper.readValue(content, MemberDto.class);

    Assertions.assertEquals("userLogged", response.name());
  }

  @Test
  @DisplayName("Get member by ID that doesn't exist")
  void getMemberByIdNotExists() throws Exception {
    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            get("/v1/members/" + new Random().nextInt())
                .header("Authorization", "Bearer " + adminToken))
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

    if (adminToken == null) loginWithAdmin();

    var movementResults =
        mockMvc
            .perform(
                get("/v1/members/" + memberEntity.getId() + "/movements")
                    .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());

    var memberResult =
        mockMvc
            .perform(
                get("/v1/members/" + memberEntity.getId())
                    .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());

    var movementContent = movementResults.andReturn().getResponse().getContentAsString();
    List<MovementDto> movements = objectMapper.readValue(movementContent, new TypeReference<>() {});

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
                .memberId(123456)
                .type(MovementType.EXPENSE)
                .description("")
                .amount(-30)
                .build(),
            MovementEntity.builder()
                .memberId(123456)
                .type(MovementType.INCOME)
                .description("")
                .amount(20)
                .build()));

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            get("/v1/members/123456/movements").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isNotFound());

    movementRepository.deleteAll();
  }

  @Test
  @DisplayName("Create member")
  void createMember() throws Exception {
    var member = new CreateMemberDto("test", "test", "test", "", Role.USER);

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            post("/v1/members")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(member)))
        .andExpect(status().isCreated());

    Assertions.assertTrue(memberRepository.findByNameAndSurname("test", "test").isPresent());

    memberRepository.delete(memberRepository.findByNameAndSurname("test", "test").get());
  }

  @Test
  @DisplayName("Create member already exists")
  void createMemberAlreadyExits() throws Exception {
    var member = new CreateMemberDto("test", "test", "test", "", Role.USER);
    var entity =
        memberRepository.save(
            MemberEntity.builder()
                .role(member.role())
                .phone(member.phone())
                .password("test")
                .name(member.name())
                .surname(member.surname())
                .build());

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            post("/v1/members")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(member)))
        .andExpect(status().isBadRequest());

    memberRepository.delete(entity);
  }

  @Test
  @DisplayName("Create member without admin role")
  void createMemberWithoutAdminRole() throws Exception {
    var member = new CreateMemberDto("test", "test", "test", "", Role.USER);

    if (token == null) loginWithUser("User1");

    mockMvc
        .perform(
            post("/v1/members")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(member)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Update member")
  void updateMember() throws Exception {
    var entity =
        memberRepository.save(
            MemberEntity.builder()
                .role(Role.USER)
                .phone("")
                .password("test")
                .name("test")
                .surname("test")
                .build());
    var updateDto = new UpdateMemberDto("test2", "test2", "test2", "1", Role.ADMIN, 10);

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            put("/v1/members/" + entity.getId())
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk());

    var member = memberRepository.findByNameAndSurname("test2", "test2");
    Assertions.assertTrue(member.isPresent());
    Assertions.assertEquals(Role.ADMIN, member.get().getRole());
    Assertions.assertEquals(10, member.get().getNCaptaincies());
    Assertions.assertEquals("1", member.get().getPhone());

    memberRepository.deleteById(entity.getId());
  }

  @Test
  @DisplayName("Update member doesn't exist")
  void updateMemberNotExist() throws Exception {
    var updateDto = new UpdateMemberDto("test2", "test2", "test2", "1", Role.ADMIN, 10);

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            put("/v1/members/" + new Random().nextInt())
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Update member without admin role")
  void updateMemberWithoutAdminRole() throws Exception {
    var updateDto = new UpdateMemberDto("test2", "test2", "test2", "1", Role.ADMIN, 10);

    if (token == null) loginWithUser("User2");

    mockMvc
        .perform(
            put("/v1/members/" + new Random().nextInt())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Update member password")
  void updateMemberPassword() throws Exception {
    var updateDto = new UpdatePasswordDto("test2");

    if (token == null) loginWithUser("user5");

    mockMvc
        .perform(
            patch("/v1/members/me/password")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Update logged member password")
  void updateLoggedMemberPassword() throws Exception {
    var updateDto = new UpdatePasswordDto("test2");

    if (token == null) loginWithUser("User4");

    var member =
        memberRepository
            .findByNameAndSurname("User4", "User4")
            .orElseThrow(() -> new MemberNotFoundException(""));

    mockMvc
        .perform(
            patch("/v1/members/me/password")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk());

    var entityUpdated =
        memberRepository
            .findById(member.getId())
            .orElseThrow(() -> new MemberNotFoundException(""));
    Assertions.assertNotEquals("test", entityUpdated.getPassword());
  }

  @Test
  @DisplayName("Update logged member alias")
  void updateLoggedMemberAlias() throws Exception {
    var updateDto = new UpdateAliasDto("alias");

    if (token == null) loginWithUser("User5");

    var member =
        memberRepository
            .findByNameAndSurname("User5", "User5")
            .orElseThrow(() -> new MemberNotFoundException(""));

    mockMvc
        .perform(
            patch("/v1/members/me/alias")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk());

    var entityUpdated =
        memberRepository
            .findById(member.getId())
            .orElseThrow(() -> new MemberNotFoundException(""));
    Assertions.assertEquals("alias", entityUpdated.getAlias());
  }

  @Test
  @DisplayName("Update member injured status")
  void updateMemberInjuredStatus() throws Exception {
    var entity =
        memberRepository.save(
            MemberEntity.builder()
                .role(Role.USER)
                .phone("")
                .password("test")
                .name("test")
                .surname("test")
                .injured(false)
                .build());
    var request = new RequestInjuredDto(true);

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            patch("/v1/members/" + entity.getId() + "/injured")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    var member = memberRepository.findById(entity.getId());
    Assertions.assertTrue(member.isPresent());
    Assertions.assertTrue(member.get().isInjured());

    memberRepository.deleteById(entity.getId());
  }

  @Test
  @DisplayName("Update member injured status")
  void updateMemberBlockedStatus() throws Exception {
    var entity =
        memberRepository.save(
            MemberEntity.builder()
                .role(Role.USER)
                .phone("")
                .password("test")
                .name("test")
                .surname("test")
                .injured(false)
                .blocked(false)
                .build());
    var request = new RequestBlockedDto(true);

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            patch("/v1/members/" + entity.getId() + "/blocked")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    var member = memberRepository.findById(entity.getId());
    Assertions.assertTrue(member.isPresent());
    Assertions.assertTrue(member.get().isBlocked());

    memberRepository.deleteById(entity.getId());
  }

  @Test
  @DisplayName("Delete member")
  void deleteMember() throws Exception {
    var entity =
        memberRepository.save(
            MemberEntity.builder()
                .role(Role.USER)
                .phone("")
                .password("test")
                .name("test")
                .surname("test")
                .build());

    if (adminToken == null) loginWithAdmin();

    mockMvc
        .perform(
            delete("/v1/members/" + entity.getId()).header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk());

    var member = memberRepository.findById(entity.getId());
    Assertions.assertTrue(member.isEmpty());
  }

  @Test
  @DisplayName("Delete member no admin role")
  void deleteMemberNotAdminRole() throws Exception {
    var entity =
        memberRepository.save(
            MemberEntity.builder()
                .role(Role.USER)
                .phone("")
                .password("test")
                .name("test")
                .surname("test")
                .build());

    if (token == null) loginWithUser("User6");

    mockMvc
        .perform(delete("/v1/members/" + entity.getId()).header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }
}
