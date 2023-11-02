package com.torresj.footballteammanagementapi.services;

import com.torresj.footballteammanagementapi.dtos.MovementDto;
import com.torresj.footballteammanagementapi.dtos.TotalBalanceDto;
import com.torresj.footballteammanagementapi.enums.MovementType;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.exceptions.MovementNotFoundException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MovementService {
  Page<MovementDto> get(Long memberId, String filter, int nElements, int nPage);

  MovementDto get(long id) throws MovementNotFoundException;

  List<MovementDto> getByMember(long memberId) throws MemberNotFoundException;

  double getBalance(long memberId);

  MovementDto create(long memberId, MovementType type, double amount, String description)
      throws MemberNotFoundException;

  MovementDto update(long id, double amount, String description)
      throws MovementNotFoundException, MemberNotFoundException;

  void delete(long id);

  void addAnnualTeamPay();

  TotalBalanceDto getTotalBalance();
}
