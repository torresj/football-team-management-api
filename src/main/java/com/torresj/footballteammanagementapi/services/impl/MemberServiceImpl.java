package com.torresj.footballteammanagementapi.services.impl;

import com.torresj.footballteammanagementapi.dtos.MemberDto;
import com.torresj.footballteammanagementapi.entities.MemberEntity;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.security.CustomUserDetails;
import com.torresj.footballteammanagementapi.services.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService, UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public MemberDto get(long id) {
    return null;
  }

  @Override
  public MemberDto update(String name, String surname, String phone) {
    return null;
  }

  @Override
  public MemberDto create(String name, String surname, String phone) {
    return null;
  }

  @Override
  public void delete(long id) {}

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
