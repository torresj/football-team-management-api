package com.torresj.footballteammanagementapi.dtos;

import com.torresj.footballteammanagementapi.enums.Role;

import java.time.LocalDate;

public record CreateMatchDto(LocalDate matchDay) {}
