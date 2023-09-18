package com.torresj.footballteammanagementapi.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.footballteammanagementapi.dtos.RequestLoginDto;
import com.torresj.footballteammanagementapi.dtos.ResponseLoginDto;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.enums.Role;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.services.JwtService;

import java.time.Instant;

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

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder encoder;

    @Value("${admin.user}")
    private String adminUser;

    @Value("${admin.password}")
    private String adminPassword;

    @Test
    @DisplayName("Login with an admin user")
    void loginWithAdmin() throws Exception {
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
                                                                Instant.now().getEpochSecond()))))
                        .andExpect(status().isOk());
        var content = result.andReturn().getResponse().getContentAsString();
        ResponseLoginDto response = objectMapper.readValue(content, ResponseLoginDto.class);

        Assertions.assertEquals(adminUser + "." + adminUser, jwtService.validateJWS(response.jwt()));
    }

    @Test
    @DisplayName("Login with a non admin user")
    void loginWithUser() throws Exception {
        var member = memberRepository.save(
                MemberEntity.builder()
                        .name("loginNotAdminTest")
                        .surname("test")
                        .phone("")
                        .role(Role.USER)
                        .password(encoder.encode("test"))
                        .build());
        var result =
                mockMvc
                        .perform(
                                MockMvcRequestBuilders.post("/v1/login")
                                        .accept(MediaType.APPLICATION_JSON)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        new RequestLoginDto(
                                                                "loginNotAdminTest.test", "test", Instant.now().getEpochSecond()))))
                        .andExpect(status().isOk());
        var content = result.andReturn().getResponse().getContentAsString();
        ResponseLoginDto response = objectMapper.readValue(content, ResponseLoginDto.class);

        Assertions.assertEquals("loginNotAdminTest.test", jwtService.validateJWS(response.jwt()));

        memberRepository.delete(member);
    }

    @Test
    @DisplayName("Login with a user not found")
    void loginWithUserNotFound() throws Exception {
        var result =
                mockMvc
                        .perform(
                                MockMvcRequestBuilders.post("/v1/login")
                                        .accept(MediaType.APPLICATION_JSON)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        new RequestLoginDto(
                                                                "test.test", "test", Instant.now().getEpochSecond()))))
                        .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Login with a wrong password")
    void loginWithWrongPassword() throws Exception {
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
                                                                "wrong password",
                                                                Instant.now().getEpochSecond()))))
                        .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Login with a nonce already used")
    void loginWithNonceAlreadyUsed() throws Exception {
        var member =
                memberRepository.save(
                        MemberEntity.builder()
                                .name("test")
                                .surname("test")
                                .phone("")
                                .role(Role.USER)
                                .password(encoder.encode("test"))
                                .nonce(Instant.now().plusSeconds(100).getEpochSecond())
                                .build());
        var result =
                mockMvc
                        .perform(
                                MockMvcRequestBuilders.post("/v1/login")
                                        .accept(MediaType.APPLICATION_JSON)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        new RequestLoginDto(
                                                                "test.test", "test", Instant.now().getEpochSecond()))))
                        .andExpect(status().isForbidden());

        memberRepository.delete(member);
    }
}
