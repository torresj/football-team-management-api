package com.torresj.footballteammanagementapi.security;

import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.enums.Role;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminConfigUser {

  private final MemberRepository memberRepository;
  private final PasswordEncoder encoder;

  @Value("${admin.user}")
  private final String adminUser;

  @Value("${admin.password}")
  private final String adminPassword;

  @Bean
  void createAdminUser() {
    var member = memberRepository.findByNameAndSurname(adminUser, adminUser);
    member.ifPresent(memberRepository::delete);
    memberRepository.save(
        MemberEntity.builder()
            .name(adminUser)
            .surname(adminUser)
            .password(encoder.encode(adminPassword))
            .role(Role.ADMIN)
            .phone("")
            .blocked(false)
            .injured(false)
            .build());
  }
}
