package com.torresj.footballteammanagementapi.controllers;

import com.torresj.footballteammanagementapi.dtos.*;
import com.torresj.footballteammanagementapi.exceptions.MemberAlreadyExistsException;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.services.MemberService;
import com.torresj.footballteammanagementapi.services.MovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.security.Principal;
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
    private final MovementService movementService;
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

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get logged member")
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
    ResponseEntity<MemberDto> getMe(Principal principal)
            throws MemberNotFoundException {
        log.info("[MEMBERS] Getting member " + principal.getName());
        var member = memberService.get(principal.getName());
        log.info("[MEMBERS] Member found");
        return ResponseEntity.ok(member);
    }

    @GetMapping("/{id}/movements")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get movements by member ID")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Movements found",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = MovementDto.class)))
                            }),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<List<MovementDto>> getMovements(
            @Parameter(description = "Member id") @PathVariable long id) throws MemberNotFoundException {
        log.info("[MEMBERS] Getting movements for member " + id);
        var movements = movementService.getByMember(id);
        log.info("[MEMBERS] Movements found");
        return ResponseEntity.ok(movements);
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
    ResponseEntity<Void> create(
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
                        request.alias(),
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
        memberService.update(
                id,
                request.name(),
                request.alias(),
                request.surname(),
                request.phone(),
                request.nCaptaincies(),
                request.role());
        log.info("[MEMBERS] Member updated");
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/password")
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
    ResponseEntity<Void> updateMyPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Update Member password",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdatePasswordDto.class)))
            @RequestBody
            UpdatePasswordDto request,
            Principal principal)
            throws MemberNotFoundException {
        log.info("[MEMBERS] Updating user " + principal.getName());
        memberService.updateMyPassword(principal.getName(), encoder.encode(request.newPassword()));
        log.info("[MEMBERS] Member updated");
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/alias")
    @Operation(summary = "Update Member alias")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Alias updated",
                            content = {@Content()}),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Member Not Found",
                            content = {@Content()})
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> updateMyAlias(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Update Member alias",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateAliasDto.class)))
            @RequestBody
            UpdateAliasDto request,
            Principal principal)
            throws MemberNotFoundException {
        log.info("[MEMBERS] Updating user " + principal.getName());
        memberService.updateMyAlias(principal.getName(), request.alias());
        log.info("[MEMBERS] Member updated");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete member")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "member deleted",
                            content = {@Content()}),
                    @ApiResponse(
                            responseCode = "404",
                            description = "member not found",
                            content = {@Content()})
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> delete(@Parameter(description = "Member id") @PathVariable long id) {
        log.info("[MEMBERS] Deleting member " + id);
        memberService.delete(id);
        log.info("[MEMBERS] Member " + id + " deleted");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @PatchMapping("/{id}/injured")
    @Operation(summary = "Change injured status of a member")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "member updated",
                            content = {@Content()}),
                    @ApiResponse(
                            responseCode = "404",
                            description = "member not found",
                            content = {@Content()})
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> updateInjuredStatus(
            @Parameter(description = "Member id") @PathVariable long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Update Member",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RequestInjuredDto.class)))
            @RequestBody
            RequestInjuredDto request) throws MemberNotFoundException {
        log.info("[MEMBERS] Updating member " + id + " injured status to ");
        memberService.setInjured(id, request.injured());
        log.info("[MEMBERS] Member " + id + " injured status updated");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @PatchMapping("/{id}/blocked")
    @Operation(summary = "Change blocked status of a member")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "member updated",
                            content = {@Content()}),
                    @ApiResponse(
                            responseCode = "404",
                            description = "member not found",
                            content = {@Content()})
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> updateBlockedStatus(
            @Parameter(description = "Member id") @PathVariable long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Update Member",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RequestBlockedDto.class)))
            @RequestBody
            RequestBlockedDto request) throws MemberNotFoundException {
        log.info("[MEMBERS] Updating member " + id + " blocked status to ");
        memberService.setBlocked(id, request.blocked());
        log.info("[MEMBERS] Member " + id + " blocked status updated");
        return ResponseEntity.ok().build();
    }
}
