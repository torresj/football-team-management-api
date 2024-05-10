package com.torresj.footballteammanagementapi.controllers;

import com.torresj.footballteammanagementapi.dtos.GuestRequestDto;
import com.torresj.footballteammanagementapi.dtos.AddPlayerRequestDto;
import com.torresj.footballteammanagementapi.dtos.CreateMatchDto;
import com.torresj.footballteammanagementapi.dtos.MatchDto;
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

import java.security.Principal;
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

    @Secured("ROLE_ADMIN")
    @PostMapping("/{id}/close")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Close match by ID")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Match closed",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> close(@Parameter(description = "Match id") @PathVariable long id)
            throws MatchNotFoundException {
        log.info("[MATCHES] Closing match " + id);
        matchService.close(id);
        log.info("[MATCHES] Match closed");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/players")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add logged player to match by ID")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Player added",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> addPlayer(@Parameter(description = "Match id") @PathVariable long id,
                                   @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                           description = "Player status for this match",
                                           required = true,
                                           content = @Content(schema = @Schema(implementation = AddPlayerRequestDto.class)))
                                   @RequestBody AddPlayerRequestDto request, Principal principal)
            throws MatchNotFoundException, MemberNotFoundException, MemberBlockedException {
        log.info("[MATCHES] Adding player " + principal.getName() + " to match " + id);
        matchService.addPlayer(id, request.status(), principal.getName());
        log.info("[MATCHES] Player added");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/{matchId}/players/{playerId}/teama")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add player to team A")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Player added",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> addPlayerToTeamA(
            @Parameter(description = "Match id") @PathVariable long matchId,
            @Parameter(description = "Player id") @PathVariable long playerId)
            throws MatchNotFoundException, MemberNotFoundException, PlayerUnavailableException {
        log.info("[MATCHES] Adding player " + playerId + " to team A");
        matchService.addPlayerToTeamA(matchId, playerId);
        log.info("[MATCHES] Player added");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/{matchId}/players/{playerId}/teamb")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add player to team B")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Player added",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> addPlayerToTeamB(
            @Parameter(description = "Match id") @PathVariable long matchId,
            @Parameter(description = "Player id") @PathVariable long playerId)
            throws MatchNotFoundException, MemberNotFoundException, PlayerUnavailableException {
        log.info("[MATCHES] Adding player " + playerId + " to team B");
        matchService.addPlayerToTeamB(matchId, playerId);
        log.info("[MATCHES] Player added");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{matchId}/players/{playerId}/teama")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete player from team A")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Player removed",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> deletePlayerFromTeamA(
            @Parameter(description = "Match id") @PathVariable long matchId,
            @Parameter(description = "Player id") @PathVariable long playerId)
            throws MatchNotFoundException {
        log.info("[MATCHES] Removing player " + playerId + " from team A");
        matchService.removePlayerFromTeamA(matchId, playerId);
        log.info("[MATCHES] Player removed");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{matchId}/players/{playerId}/teamb")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete player from team B")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Player removed",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> deletePlayerFromTeamB(
            @Parameter(description = "Match id") @PathVariable long matchId,
            @Parameter(description = "Player id") @PathVariable long playerId)
            throws MatchNotFoundException {
        log.info("[MATCHES] Removing player " + playerId + " from team B");
        matchService.removePlayerFromTeamB(matchId, playerId);
        log.info("[MATCHES] Player removed");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/{matchId}/guests/teama")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add guest to team A")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Guest added",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> addGuestToTeamA(
            @Parameter(description = "Match id") @PathVariable long matchId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Add guest",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GuestRequestDto.class)))
            @RequestBody GuestRequestDto request) throws MatchNotFoundException {
        log.info("[MATCHES] Adding guest " + request.guest() + " to team A");
        matchService.addGuestToTeamA(matchId, request.guest());
        log.info("[MATCHES] Guest added");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/{matchId}/guests/teamb")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add guest to team B")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Guest added",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> addGuestToTeamB(
            @Parameter(description = "Match id") @PathVariable long matchId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Add guest",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GuestRequestDto.class)))
            @RequestBody GuestRequestDto request) throws MatchNotFoundException {
        log.info("[MATCHES] Adding guest " + request.guest() + " to team B");
        matchService.addGuestToTeamB(matchId, request.guest());
        log.info("[MATCHES] Guest added");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{matchId}/guests/teama")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete guest from team A")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Guest removed",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> deleteGuestFromTeamA(
            @Parameter(description = "Match id") @PathVariable long matchId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Remove guest",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GuestRequestDto.class)))
            @RequestBody GuestRequestDto request)
            throws MatchNotFoundException {
        log.info("[MATCHES] Removing guest " + request.guest() + " from team A");
        matchService.removeGuestFromTeamA(matchId, request.guest());
        log.info("[MATCHES] Guest removed");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{matchId}/guests/teamb")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete guest from team B")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Guest removed",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> deleteGuestFromTeamB(
            @Parameter(description = "Match id") @PathVariable long matchId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Remove guest",
                    required = true,
                    content = @Content(schema = @Schema(implementation = GuestRequestDto.class)))
            @RequestBody GuestRequestDto request)
            throws MatchNotFoundException {
        log.info("[MATCHES] Removing guest " + request.guest() + " from team B");
        matchService.removeGuestFromTeamB(matchId, request.guest());
        log.info("[MATCHES] Guest removed");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/{matchId}/captainA")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add random player as captain to team A")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Captain added",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> addPlayerAsCaptainToTeamA(
            @Parameter(description = "Match id") @PathVariable long matchId)
            throws MatchNotFoundException {
        log.info("[MATCHES] Adding random player to team A as captain");
        matchService.setRandomCaptainTeamA(matchId);
        log.info("[MATCHES] Player added");
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/{matchId}/captainB")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add random player as captain to team A")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Captain added",
                            content = {@Content()}),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            })
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<Void> addPlayerAsCaptainToTeamB(
            @Parameter(description = "Match id") @PathVariable long matchId)
            throws MatchNotFoundException {
        log.info("[MATCHES] Adding random player to team B as captain");
        matchService.setRandomCaptainTeamB(matchId);
        log.info("[MATCHES] Player added");
        return ResponseEntity.ok().build();
    }
}
