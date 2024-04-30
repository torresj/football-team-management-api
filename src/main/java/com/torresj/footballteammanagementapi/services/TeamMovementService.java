package com.torresj.footballteammanagementapi.services;

import com.torresj.footballteammanagementapi.dtos.MovementDto;
import com.torresj.footballteammanagementapi.dtos.TotalBalanceDto;
import com.torresj.footballteammanagementapi.enums.MovementType;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.exceptions.MovementNotFoundException;
import java.util.List;
import org.springframework.data.domain.Page;

public interface TeamMovementService {
  List<MovementDto> get();

  MovementDto get(long id) throws MovementNotFoundException;

  MovementDto create(MovementType type, double amount, String description)
      throws MemberNotFoundException;

  MovementDto update(long id, double amount, String description)
      throws MovementNotFoundException, MemberNotFoundException;

  void delete(long id);

  TotalBalanceDto getTotalBalance();
}
