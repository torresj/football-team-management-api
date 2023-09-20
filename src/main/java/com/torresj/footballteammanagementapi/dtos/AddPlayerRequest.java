package com.torresj.footballteammanagementapi.dtos;

import com.torresj.footballteammanagementapi.enums.PlayerMatchStatus;

public record AddPlayerRequest(PlayerMatchStatus status) {
}
