package com.torresj.footballteammanagementapi.controllers;

import com.torresj.footballteammanagementapi.dtos.CreateMemberDto;
import com.torresj.footballteammanagementapi.dtos.MemberDto;
import com.torresj.footballteammanagementapi.dtos.UpdateMemberDto;
import com.torresj.footballteammanagementapi.dtos.UpdatePasswordDto;
import com.torresj.footballteammanagementapi.exceptions.MemberAlreadyExistsException;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.services.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("v1/members")
@Slf4j
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;
  private final PasswordEncoder encoder;

  @Value("${default.password}")
  private final String defaultPassword;

  @GetMapping
  @SecurityRequirement(name = "Bearer Authentication")
  @Operation(summary = "Get all members")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Members returned",
            content = {
              @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = MemberDto.class)))
            })
      })
  ResponseEntity<List<MemberDto>> getAll() {
    log.info("[MEMBERS] Getting members ...");
    var members = memberService.get();
    log.info("[MEMBERS] Members found: " + members.size());
    return ResponseEntity.ok(members);
  }

  @GetMapping("/{id}")
  @SecurityRequirement(name = "Bearer Authentication")
  @Operation(summary = "Get member by ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Member found",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = MemberDto.class))
            }),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
      })
  @SecurityRequirement(name = "Bearer Authentication")
  ResponseEntity<MemberDto> get(@Parameter(description = "Member id") @PathVariable long id)
      throws MemberNotFoundException {
    log.info("[MEMBERS] Getting member " + id);
    var member = memberService.get(id);
    log.info("[MEMBERS] Member found");
    return ResponseEntity.ok(member);
  }

  @Secured("ROLE_ADMIN")
  @PostMapping
  @Operation(summary = "Create Member")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Member created",
            content = {@Content()}),
        @ApiResponse(
            responseCode = "400",
            description = "Member already exists",
            content = {@Content()})
      })
  @SecurityRequirement(name = "Bearer Authentication")
  ResponseEntity<MemberDto> create(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Create Member",
              required = true,
              content = @Content(schema = @Schema(implementation = CreateMemberDto.class)))
          @RequestBody
          CreateMemberDto request)
      throws MemberAlreadyExistsException {
    log.info("[MEMBERS] Crating new user " + request.name() + " " + request.surname());
    var member =
        memberService.create(
            request.name(),
            request.surname(),
            request.phone(),
            encoder.encode(defaultPassword),
            request.role());
    log.info("[MEMBERS] Member created");
    return ResponseEntity.created(
            ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/v1/members/" + member.id())
                .build()
                .toUri())
        .build();
  }

  @Secured("ROLE_ADMIN")
  @PutMapping("/{id}")
  @Operation(summary = "Update Member")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Member updated",
            content = {@Content()}),
        @ApiResponse(
            responseCode = "404",
            description = "Member Not Found",
            content = {@Content()})
      })
  @SecurityRequirement(name = "Bearer Authentication")
  ResponseEntity<Void> update(
      @Parameter(description = "Member id") @PathVariable long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Update Member",
              required = true,
              content = @Content(schema = @Schema(implementation = UpdateMemberDto.class)))
          @RequestBody
          UpdateMemberDto request)
      throws MemberNotFoundException {
    log.info("[MEMBERS] Updating user " + request.name() + " " + request.surname());
    var member =
        memberService.update(
            id,
            request.name(),
            request.surname(),
            request.phone(),
            request.nCaptaincies(),
            request.role());
    log.info("[MEMBERS] Member updated");
    return ResponseEntity.ok().build();
  }

  @Secured("ROLE_ADMIN")
  @PatchMapping("/{id}")
  @Operation(summary = "Update Member password")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password updated",
            content = {@Content()}),
        @ApiResponse(
            responseCode = "404",
            description = "Member Not Found",
            content = {@Content()})
      })
  @SecurityRequirement(name = "Bearer Authentication")
  ResponseEntity<Void> updatePassword(
      @Parameter(description = "Member id") @PathVariable long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Update Member password",
              required = true,
              content = @Content(schema = @Schema(implementation = UpdatePasswordDto.class)))
          @RequestBody
          UpdatePasswordDto request)
      throws MemberNotFoundException {
    log.info("[MEMBERS] Updating user " +id);
    memberService.updatePassword(id,encoder.encode(request.newPassword()));
    log.info("[MEMBERS] Member updated");
    return ResponseEntity.ok().build();
  }

  @Secured("ROLE_ADMIN")
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete template")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "template deleted",
            content = {@Content()}),
        @ApiResponse(
            responseCode = "404",
            description = "Template not found",
            content = {@Content()})
      })
  @SecurityRequirement(name = "Bearer Authentication")
  ResponseEntity<Void> delete(@Parameter(description = "Member id") @PathVariable long id) {
    log.info("[MEMBERS] Deleting member " + id);
    memberService.delete(id);
    log.info("[MEMBERS] Member " + id + " deleted");
    return ResponseEntity.ok().build();
  }
}
