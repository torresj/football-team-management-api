package com.torresj.footballteammanagementapi.services.impl;

import com.torresj.footballteammanagementapi.dtos.MemberDto;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.enums.Role;
import com.torresj.footballteammanagementapi.exceptions.MemberAlreadyExistsException;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.security.CustomUserDetails;
import com.torresj.footballteammanagementapi.services.MemberService;
import com.torresj.footballteammanagementapi.services.MovementService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService, UserDetailsService {

    private final MemberRepository memberRepository;
    private final MovementService movementService;

    @Value("${admin.user}")
    private final String adminUser;

    @Override
    public MemberDto get(long id) throws MemberNotFoundException {
        var member = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(""));
        return new MemberDto(
                member.getId(),
                member.getName(),
                member.getSurname(),
                member.getPhone(),
                member.getNCaptaincies(),
                member.getRole(),
                movementService.getBalance(id),
                member.isInjured());
    }

    @Override
    public void setInjured(long id, boolean injured) throws MemberNotFoundException {
        var member = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(""));
        memberRepository.save(
                MemberEntity.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .surname(member.getSurname())
                        .phone(member.getPhone())
                        .role(member.getRole())
                        .nCaptaincies(member.getNCaptaincies())
                        .nonce(member.getNonce())
                        .password(member.getPassword())
                        .injured(injured)
                        .build());
    }

    @Override
    public MemberDto get(String username) throws MemberNotFoundException {
        if (username.split("\\.").length != 2) {
            throw new MemberNotFoundException(username);
        }
        var member =
                memberRepository
                        .findByNameAndSurname(username.split("\\.")[0], username.split("\\.")[1])
                        .orElseThrow(() -> new MemberNotFoundException(""));
        return new MemberDto(
                member.getId(),
                member.getName(),
                member.getSurname(),
                member.getPhone(),
                member.getNCaptaincies(),
                member.getRole(),
                movementService.getBalance(member.getId()),
                member.isInjured());
    }

    @Override
    public List<MemberDto> get() {
        return memberRepository.findAll().stream()
                .map(
                        entity ->
                                new MemberDto(
                                        entity.getId(),
                                        entity.getName(),
                                        entity.getSurname(),
                                        entity.getPhone(),
                                        entity.getNCaptaincies(),
                                        entity.getRole(),
                                        movementService.getBalance(entity.getId()),
                                        entity.isInjured()))
                .filter(member -> !adminUser.equals(member.name()))
                .toList();
    }

    @Override
    public MemberDto update(
            long id, String name, String surname, String phone, int nCaptaincies, Role role)
            throws MemberNotFoundException {
        var member = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(""));
        var memberUpdated = memberRepository.save(
                MemberEntity.builder()
                        .id(member.getId())
                        .name(name)
                        .surname(surname)
                        .phone(phone)
                        .role(role)
                        .nCaptaincies(nCaptaincies)
                        .nonce(member.getNonce())
                        .password(member.getPassword())
                        .injured(member.isInjured())
                        .build());
        return new MemberDto(
                memberUpdated.getId(),
                name,
                surname,
                phone,
                nCaptaincies,
                role,
                movementService.getBalance(id),
                member.isInjured());
    }

    @Override
    public MemberDto create(String name, String surname, String phone, String password, Role role)
            throws MemberAlreadyExistsException {
        if (memberRepository.findByNameAndSurname(name, surname).isPresent())
            throw new MemberAlreadyExistsException(name);

        var member =
                memberRepository.save(
                        MemberEntity.builder()
                                .name(name)
                                .surname(surname)
                                .phone(phone)
                                .role(role)
                                .password(password)
                                .injured(false)
                                .build());

        return new MemberDto(
                member.getId(),
                member.getName(),
                member.getSurname(),
                member.getPhone(),
                member.getNCaptaincies(),
                member.getRole(),
                movementService.getBalance(member.getId()),
                member.isInjured());
    }

    @Override
    public void updateMyPassword(String user, String newPassword) throws MemberNotFoundException {
        if (user.split("\\.").length != 2) {
            throw new MemberNotFoundException(user);
        }
        var member =
                memberRepository
                        .findByNameAndSurname(user.split("\\.")[0], user.split("\\.")[1])
                        .orElseThrow(() -> new MemberNotFoundException(""));
        memberRepository.save(
                MemberEntity.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .surname(member.getSurname())
                        .password(newPassword)
                        .nonce(member.getNonce())
                        .nCaptaincies(member.getNCaptaincies())
                        .role(member.getRole())
                        .phone(member.getPhone())
                        .injured(member.isInjured())
                        .build());
    }

    @Override
    public void delete(long id) {
        memberRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.split("\\.").length != 2) {
            throw new UsernameNotFoundException("User not found !");
        }
        MemberEntity member =
                memberRepository
                        .findByNameAndSurname(username.split("\\.")[0], username.split("\\.")[1])
                        .orElseThrow(() -> new UsernameNotFoundException("User not found !"));
        return new CustomUserDetails(member);
    }
}
