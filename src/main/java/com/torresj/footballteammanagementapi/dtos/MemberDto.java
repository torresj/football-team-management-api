package com.torresj.footballteammanagementapi.dtos;

import com.torresj.footballteammanagementapi.enums.Role;

public record MemberDto(
    Long id,
    String name,
    String alias,
    String surname,
    String phone,
    int nCaptaincies,
    Role role,
    double balance,
    boolean injured,
    boolean blocked) {}
