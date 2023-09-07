package com.torresj.footballteammanagementapi.services;

import com.torresj.footballteammanagementapi.dtos.MovementDto;
import com.torresj.footballteammanagementapi.enums.MovementType;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.exceptions.MovementNotFoundException;
import java.util.List;

public interface MovementService {
  List<MovementDto> get();

  MovementDto get(long id) throws MovementNotFoundException;

  List<MovementDto> getByMember(long memberId) throws MemberNotFoundException;

  double getBalance(long memberId);

  MovementDto create(long memberId, MovementType type, double amount, String description)
      throws MemberNotFoundException;

  MovementDto update(long id, double amount, String description)
      throws MovementNotFoundException, MemberNotFoundException;

  void delete(long id);
}