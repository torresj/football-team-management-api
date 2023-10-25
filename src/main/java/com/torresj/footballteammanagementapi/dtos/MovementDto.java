package com.torresj.footballteammanagementapi.dtos;

import com.torresj.footballteammanagementapi.enums.MovementType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

public record MovementDto(long id, MovementType type, String memberName, double amount, String description,
                          String createdOn) {
}
