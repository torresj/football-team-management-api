package com.torresj.footballteammanagementapi.dtos;

import com.torresj.footballteammanagementapi.enums.MovementType;

import java.time.LocalDate;

public record MovementDto(long id, MovementType type, String memberName, double amount, String description,
                          LocalDate createdOn) {
}
