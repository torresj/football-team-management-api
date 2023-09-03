package com.torresj.footballteammanagementapi.services;

import com.torresj.footballteammanagementapi.dtos.MemberDto;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;

public interface MemberService {
    MemberDto get(long id);
    MemberDto update(String name, String surname, String phone);
    MemberDto create(String name, String surname, String phone);
    void delete(long id);
}
