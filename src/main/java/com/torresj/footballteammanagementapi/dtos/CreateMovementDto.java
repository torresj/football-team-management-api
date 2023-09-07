package com.torresj.footballteammanagementapi.dtos;

import com.torresj.footballteammanagementapi.enums.MovementType;

public record CreateMovementDto(MovementType type, long memberId, double amount, String description) {}
