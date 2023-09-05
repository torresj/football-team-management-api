package com.torresj.footballteammanagementapi.dtos;

public record RequestLoginDto(String username, String password, long nonce) {}
