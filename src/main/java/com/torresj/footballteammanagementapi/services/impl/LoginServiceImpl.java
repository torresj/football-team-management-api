package com.torresj.footballteammanagementapi.services.impl;

import com.torresj.footballteammanagementapi.dtos.ResponseLoginDto;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.services.JwtService;
import com.torresj.footballteammanagementapi.services.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;

    @Override
    public ResponseLoginDto login(String userName, String password, long nonce)
            throws MemberNotFoundException {
        log.debug("[LOGIN] Finding user");
        if (userName.split("\\.").length != 2) {
            throw new MemberNotFoundException(userName);
        }

        var member =
                memberRepository
                        .findByNameAndSurname(userName.split("\\.")[0], userName.split("\\.")[1])
                        .orElseThrow(() -> new MemberNotFoundException(userName));

        if (!encoder.matches(password, member.getPassword()))
            throw new MemberNotFoundException(userName);

        if (member.getNonce() >= nonce) {
            log.warn("[LOGIN] Error. Nonce already used");
            throw new MemberNotFoundException(userName);
        }

        memberRepository.save(
                MemberEntity.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .alias(member.getAlias())
                        .surname(member.getSurname())
                        .password(member.getPassword())
                        .phone(member.getPhone())
                        .role(member.getRole())
                        .nCaptaincies(member.getNCaptaincies())
                        .nonce(nonce)
                        .injured(member.isInjured())
                        .build());

        log.debug("[LOGIN] Login success. Generating JWT ...");
        String jwt = jwtService.createJWS(userName);
        return new ResponseLoginDto(jwt);
    }
}
