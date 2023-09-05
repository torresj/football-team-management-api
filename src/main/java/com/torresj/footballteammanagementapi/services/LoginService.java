package com.torresj.footballteammanagementapi.services;

import com.torresj.footballteammanagementapi.dtos.ResponseLoginDto;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;

public interface LoginService {
    ResponseLoginDto login(String username, String password, long nonce) throws MemberNotFoundException;
}
