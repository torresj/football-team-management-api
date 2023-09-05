package com.torresj.footballteammanagementapi.controllers;

import com.torresj.footballteammanagementapi.dtos.RequestLoginDto;
import com.torresj.footballteammanagementapi.dtos.ResponseLoginDto;
import com.torresj.footballteammanagementapi.services.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("v1/login")
@Slf4j
@RequiredArgsConstructor
public class LoginController {

  private final LoginService loginService;

  @PreAuthorize("permitAll()")
  @Operation(summary = "Login with user and password")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ResponseLoginDto.class))
            }),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
      })
  @PostMapping
  ResponseEntity<ResponseLoginDto> login(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Login DTO with user and password",
              required = true,
              content = @Content(schema = @Schema(implementation = RequestLoginDto.class)))
          @RequestBody
          RequestLoginDto loginDto) {
    try {
      log.info("[LOGIN] Attempt to login for " + loginDto.username());
      ResponseLoginDto response =
          loginService.login(loginDto.username(), loginDto.password(), loginDto.nonce());
      log.debug(response.jwt());
      log.info("[LOGIN] Login success");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User or password incorrect");
    }
  }
}
