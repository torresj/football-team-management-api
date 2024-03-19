package com.torresj.footballteammanagementapi.dtos;

import com.torresj.footballteammanagementapi.enums.Role;

public record UpdateMemberDto(String name, String alias, String surname, String phone, Role role, int nCaptaincies) {}
