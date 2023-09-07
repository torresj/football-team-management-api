package com.torresj.footballteammanagementapi.controllers;

import com.torresj.footballteammanagementapi.dtos.*;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.exceptions.MovementNotFoundException;
import com.torresj.footballteammanagementapi.services.MovementService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("v1/movement")
@Slf4j
@RequiredArgsConstructor
public class MovementController {

  private final MovementService movementService;

  @Secured("ROLE_ADMIN")
  @GetMapping
  @SecurityRequirement(name = "Bearer Authentication")
  @Operation(summary = "Get all movements")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Movements returned",
            content = {
              @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = MovementDto.class)))
            })
      })
  ResponseEntity<List<MovementDto>> getAll() {
    log.info("[MOVEMENTS] Getting movements ...");
    var movements = movementService.get();
    log.info("[MOVEMENTS] Movements found: " + movements.size());
    return ResponseEntity.ok(movements);
  }

  @GetMapping("/{id}")
  @SecurityRequirement(name = "Bearer Authentication")
  @Operation(summary = "Get movement by ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Movement found",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = MovementDto.class))
            }),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
      })
  @SecurityRequirement(name = "Bearer Authentication")
  ResponseEntity<MovementDto> get(@Parameter(description = "Movement id") @PathVariable long id)
      throws MovementNotFoundException {
    log.info("[MOVEMENTS] Getting member " + id);
    var movement = movementService.get(id);
    log.info("[MOVEMENTS] Movement found");
    return ResponseEntity.ok(movement);
  }

  @Secured("ROLE_ADMIN")
  @PostMapping
  @Operation(summary = "Create Movement")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Movement created",
            content = {@Content()})
      })
  @SecurityRequirement(name = "Bearer Authentication")
  ResponseEntity<MemberDto> create(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Create Movement",
              required = true,
              content = @Content(schema = @Schema(implementation = CreateMovementDto.class)))
          @RequestBody
          CreateMovementDto request)
      throws MemberNotFoundException {
    log.info("[MOVEMENTS] Crating new movement for member " + request.memberId());
    var movement =
        movementService.create(
            request.memberId(), request.type(), request.amount(), request.description());
    log.info("[MOVEMENTS] Movement created");
    return ResponseEntity.created(
            ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/v1/movements/" + movement.id())
                .build()
                .toUri())
        .build();
  }

  @Secured("ROLE_ADMIN")
  @PatchMapping("/{id}")
  @Operation(summary = "Update movement")
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Movement updated",
                          content = {@Content()}),
                  @ApiResponse(
                          responseCode = "404",
                          description = "Movement Not Found",
                          content = {@Content()})
          })
  @SecurityRequirement(name = "Bearer Authentication")
  ResponseEntity<Void> update(
          @Parameter(description = "Movement id") @PathVariable long id,
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
                  description = "Update movement",
                  required = true,
                  content = @Content(schema = @Schema(implementation = UpdateMovementDto.class)))
          @RequestBody
          UpdateMovementDto request) throws MovementNotFoundException, MemberNotFoundException {
    log.info("[MOVEMENTS] Updating movement " + id);
    movementService.update(id,request.amount(),request.description());
    log.info("[MOVEMENTS] Movement updated");
    return ResponseEntity.ok().build();
  }

  @Secured("ROLE_ADMIN")
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete movement")
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Movement deleted",
                          content = {@Content()}),
                  @ApiResponse(
                          responseCode = "404",
                          description = "Movement not found",
                          content = {@Content()})
          })
  @SecurityRequirement(name = "Bearer Authentication")
  ResponseEntity<Void> delete(@Parameter(description = "Movement id") @PathVariable long id) {
    log.info("[MOVEMENTS] Deleting movement " + id);
    movementService.delete(id);
    log.info("[MOVEMENTS] Movement " + id + " deleted");
    return ResponseEntity.ok().build();
  }
}
