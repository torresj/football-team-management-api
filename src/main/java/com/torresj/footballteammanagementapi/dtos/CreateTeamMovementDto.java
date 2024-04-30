package com.torresj.footballteammanagementapi.dtos;

import com.torresj.footballteammanagementapi.enums.MovementType;

public record CreateTeamMovementDto(MovementType type, double amount, String description) {}
