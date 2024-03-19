package com.torresj.footballteammanagementapi.dtos;

import com.torresj.footballteammanagementapi.enums.Role;

public record CreateMemberDto(String name, String alias, String surname, String phone, Role role) {}
