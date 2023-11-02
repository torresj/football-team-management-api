package com.torresj.footballteammanagementapi.dtos;

import com.torresj.footballteammanagementapi.enums.PlayerMatchStatus;

public record AddPlayerRequestDto(PlayerMatchStatus status) {
}
