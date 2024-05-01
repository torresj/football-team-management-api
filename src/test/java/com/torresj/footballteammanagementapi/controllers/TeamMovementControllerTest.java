package com.torresj.footballteammanagementapi.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.footballteammanagementapi.dtos.*;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.entities.MovementEntity;
import com.torresj.footballteammanagementapi.entities.TeamMovementEntity;
import com.torresj.footballteammanagementapi.enums.MovementType;
import com.torresj.footballteammanagementapi.enums.Role;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.repositories.MovementRepository;
import java.util.List;

import com.torresj.footballteammanagementapi.repositories.TeamMovementRepository;
import org.json.JSONObject;
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
public class TeamMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MovementRepository movementRepository;
    @Autowired
    private TeamMovementRepository teamMovementRepository;
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
    @DisplayName("Get all movements")
    void getAllMovements() throws Exception {
        teamMovementRepository.saveAll(
                List.of(
                        TeamMovementEntity.builder()
                                .amount(-10)
                                .type(MovementType.EXPENSE)
                                .description("")
                                .build(),
                        TeamMovementEntity.builder()
                                .amount(10)
                                .type(MovementType.INCOME)
                                .description("")
                                .build(),
                        TeamMovementEntity.builder()
                                .amount(-10)
                                .type(MovementType.EXPENSE)
                                .description("")
                                .build(),
                        TeamMovementEntity.builder()
                                .amount(10)
                                .type(MovementType.INCOME)
                                .description("")
                                .build()));

        if (adminToken == null) loginWithAdmin();

        var result =
                mockMvc
                        .perform(get("/v1/team/movements").header("Authorization", "Bearer " + adminToken))
                        .andExpect(status().isOk());

        var content = result.andReturn().getResponse().getContentAsString();
        List<MovementDto> movements =
                objectMapper.readValue(content, new TypeReference<>() {
                });

        Assertions.assertEquals(4, movements.size());
        teamMovementRepository.deleteAll();
    }

    @Test
    @DisplayName("Get all movements no admin user")
    void getAllMovementsNoAdmin() throws Exception {

        teamMovementRepository.saveAll(
                List.of(
                        TeamMovementEntity.builder()
                                .amount(-10)
                                .type(MovementType.EXPENSE)
                                .description("")
                                .build(),
                        TeamMovementEntity.builder()
                                .amount(10)
                                .type(MovementType.INCOME)
                                .description("")
                                .build(),
                        TeamMovementEntity.builder()
                                .amount(-10)
                                .type(MovementType.EXPENSE)
                                .description("")
                                .build(),
                        TeamMovementEntity.builder()
                                .amount(10)
                                .type(MovementType.INCOME)
                                .description("")
                                .build()));

        if (token == null) loginWithUser("teamUserTest1");

        var result =
                mockMvc
                        .perform(get("/v1/team/movements").header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk());

        var content = result.andReturn().getResponse().getContentAsString();
        List<MovementDto> movements =
                objectMapper.readValue(content, new TypeReference<>() {
                });

        Assertions.assertEquals(4, movements.size());
        teamMovementRepository.deleteAll();
    }

    @Test
    @DisplayName("Get movement by ID")
    void getMovementById() throws Exception {
        var movementEntity =
                teamMovementRepository.save(
                        TeamMovementEntity.builder()
                                .amount(10)
                                .type(MovementType.INCOME)
                                .description("")
                                .build());

        if (token == null) loginWithUser("teamUserTest2");

        mockMvc
                .perform(
                        get("/v1/team/movements/" + movementEntity.getId())
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        teamMovementRepository.delete(movementEntity);
    }

    @Test
    @DisplayName("Get a movement by ID that doesn't exist")
    void getMovementByIdNotExists() throws Exception {

        if (token == null) loginWithUser("teamUserTest3");

        mockMvc
                .perform(get("/v1/team/movements/1234").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Create movement")
    void createMovement() throws Exception {
        var movement = new CreateTeamMovementDto(MovementType.EXPENSE, -10, "");

        if (adminToken == null) loginWithAdmin();

        mockMvc
                .perform(
                        post("/v1/team/movements")
                                .header("Authorization", "Bearer " + adminToken)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(movement)))
                .andExpect(status().isCreated());

        Assertions.assertEquals(1,teamMovementRepository.findAll().size());

        teamMovementRepository.deleteAll();
    }


    @Test
    @DisplayName("Create movement without admin role")
    void createMovementNotAdmin() throws Exception {
        var movement = new CreateTeamMovementDto(MovementType.EXPENSE, -10, "");

        if (token == null) loginWithUser("teamUserTest4");

        mockMvc
                .perform(
                        post("/v1/team/movements")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(movement)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Update movement")
    void updateMovement() throws Exception {
        var entity =
                teamMovementRepository.save(
                        TeamMovementEntity.builder()
                                .amount(10)
                                .type(MovementType.INCOME)
                                .description("")
                                .build());
        var updateDto = new UpdateMovementDto(5, "test");

        if (adminToken == null) loginWithAdmin();

        mockMvc
                .perform(
                        patch("/v1/team/movements/" + entity.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        var movement = teamMovementRepository.findById(entity.getId());
        Assertions.assertTrue(movement.isPresent());
        Assertions.assertEquals("test", movement.get().getDescription());
        Assertions.assertEquals(5, movement.get().getAmount());

        teamMovementRepository.deleteById(entity.getId());
    }

    @Test
    @DisplayName("Update movement that doesn't exist")
    void updateMovementNotExist() throws Exception {
        var updateDto = new UpdateMovementDto(5, "test");

        if (adminToken == null) loginWithAdmin();

        mockMvc
                .perform(
                        patch("/v1/team/movements/1234")
                                .header("Authorization", "Bearer " + adminToken)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update movement without admin role")
    void updateMovementNotAdmin() throws Exception {
        var updateDto = new UpdateMovementDto(5, "test");

        if (token == null) loginWithUser("teamUserTest5");

        mockMvc
                .perform(
                        patch("/v1/team/movements/1234")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get total balance")
    void getTotalBalance() throws Exception {

        var movements = movementRepository.saveAll(
                List.of(
                        MovementEntity.builder()
                                .amount(-1)
                                .type(MovementType.EXPENSE)
                                .description("")
                                .memberId(1)
                                .build(),
                        MovementEntity.builder()
                                .amount(2)
                                .type(MovementType.INCOME)
                                .description("")
                                .memberId(1)
                                .build(),
                        MovementEntity.builder()
                                .amount(-3)
                                .type(MovementType.EXPENSE)
                                .description("")
                                .memberId(2)
                                .build(),
                        MovementEntity.builder()
                                .amount(7)
                                .type(MovementType.INCOME)
                                .description("")
                                .memberId(2)
                                .build()));

        var teamMovements = teamMovementRepository.saveAll(
                List.of(
                        TeamMovementEntity.builder()
                                .amount(-20)
                                .type(MovementType.EXPENSE)
                                .description("")
                                .build(),
                        TeamMovementEntity.builder()
                                .amount(2)
                                .type(MovementType.INCOME)
                                .description("")
                                .build(),
                        TeamMovementEntity.builder()
                                .amount(40)
                                .type(MovementType.INCOME)
                                .description("")
                                .build()));


        if (token == null) loginWithUser("teamUserTest6");

        var result = mockMvc
                .perform(
                        get("/v1/team/movements/balance")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        var content = result.andReturn().getResponse().getContentAsString();
        TotalBalanceDto response = objectMapper.readValue(content, TotalBalanceDto.class);

        Assertions.assertEquals(-20, response.totalExpenses());
        Assertions.assertEquals(51, response.totalIncomes());

        movementRepository.deleteAll(movements);
        teamMovementRepository.deleteAll(teamMovements);
    }

    @Test
    @DisplayName("Delete movement")
    void deleteMovement() throws Exception {
        var entity =
                teamMovementRepository.save(
                        TeamMovementEntity.builder()
                                .amount(10)
                                .type(MovementType.INCOME)
                                .description("")
                                .build());

        if (adminToken == null) loginWithAdmin();

        mockMvc
                .perform(
                        delete("/v1/team/movements/" + entity.getId())
                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        var member = teamMovementRepository.findById(entity.getId());
        Assertions.assertTrue(member.isEmpty());
    }

    @Test
    @DisplayName("Delete movement no admin role")
    void deleteMovementNotAdminRole() throws Exception {

        if (token == null) loginWithUser("teamUserTest7");

        mockMvc
                .perform(delete("/v1/team/movements/1234").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
