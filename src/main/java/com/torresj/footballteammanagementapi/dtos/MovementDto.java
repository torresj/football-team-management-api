package com.torresj.footballteammanagementapi.dtos;

import com.torresj.footballteammanagementapi.enums.MovementType;

public record MovementDto(long id, MovementType type, String memberName, double amount, String description) {}
