package com.torresj.footballteammanagementapi.controllers;

import com.torresj.footballteammanagementapi.dtos.CreateMatchDto;
import com.torresj.footballteammanagementapi.dtos.CreateMemberDto;
import com.torresj.footballteammanagementapi.dtos.MatchDto;
import com.torresj.footballteammanagementapi.dtos.MemberDto;
import com.torresj.footballteammanagementapi.exceptions.*;
import com.torresj.footballteammanagementapi.services.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("v1/matches")
@Slf4j
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all matches")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Matches returned",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = MatchDto.class)))
                            })
            })
    ResponseEntity<List<MatchDto>> getAll() {
        log.info("[MATCHES] Getting matches ...");
        var matches = matchService.get();
        log.info("[MATCHES] Matches found: " + matches.size());
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get match by ID")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Match found",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = MatchDto.class))
                            }),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<MatchDto> get(@Parameter(description = "Match id") @PathVariable long id)
            throws MatchNotFoundException {
        log.info("[MATCHES] Getting match " + id);
        var match = matchService.get(id);
        log.info("[MATCHES] Match found");
        return ResponseEntity.ok(match);
    }

    @GetMapping("/next")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get next match")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Match found",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = MatchDto.class))
                            }),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<MatchDto> get()
            throws NextMatchException {
        log.info("[MATCHES] Getting next match ");
        var match = matchService.getNext();
        log.info("[MATCHES] Next match found");
        return ResponseEntity.ok(match);
    }

    @Secured("ROLE_ADMIN")
    @PostMapping
    @Operation(summary = "Create Match")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Match created",
                            content = {@Content()}),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Match already exists",
                            content = {@Content()})
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Create Match",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateMatchDto.class)))
            @RequestBody
            CreateMatchDto request) throws MatchAlreadyExistsException {
        log.info("[MATCHES] Crating new match for " + request.matchDay().toString());
        var match = matchService.create(request.matchDay());
        log.info("[MATCHES] Match created");
        return ResponseEntity.created(
                        ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/v1/matches/" + match.id())
                                .build()
                                .toUri())
                .build();
    }
}
