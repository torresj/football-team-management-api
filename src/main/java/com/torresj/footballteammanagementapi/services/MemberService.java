package com.torresj.footballteammanagementapi.services;

import com.torresj.footballteammanagementapi.dtos.MemberDto;
import com.torresj.footballteammanagementapi.enums.Role;
import com.torresj.footballteammanagementapi.exceptions.MemberAlreadyExistsException;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;

import java.util.List;

public interface MemberService {
    MemberDto get(long id) throws MemberNotFoundException;
    void setInjured(long id, boolean injured) throws MemberNotFoundException;
    MemberDto get(String username) throws MemberNotFoundException;
    List<MemberDto> get();
    MemberDto update(long id, String name, String surname, String phone, int nCaptaincies, Role role) throws MemberNotFoundException;
    MemberDto create(String name, String surname, String phone, String password, Role role) throws MemberAlreadyExistsException;
    void updatePassword(long id, String newPassword) throws MemberNotFoundException;
    void updateMyPassword(String user, String newPassword) throws MemberNotFoundException;
    void delete(long id);
}
