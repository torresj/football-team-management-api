package com.torresj.footballteammanagementapi.services.impl;

import com.torresj.footballteammanagementapi.dtos.MovementDto;
import com.torresj.footballteammanagementapi.dtos.TotalBalanceDto;
import com.torresj.footballteammanagementapi.entities.MovementEntity;
import com.torresj.footballteammanagementapi.entities.TeamMovementEntity;
import com.torresj.footballteammanagementapi.enums.MovementType;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.exceptions.MovementNotFoundException;
import com.torresj.footballteammanagementapi.repositories.MovementRepository;
import com.torresj.footballteammanagementapi.repositories.TeamMovementRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.torresj.footballteammanagementapi.services.TeamMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamMovementServiceImpl implements TeamMovementService {

    private final TeamMovementRepository teamMovementRepository;
    private final MovementRepository movementRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Override
    public List<MovementDto> get() {
        return teamMovementRepository.findByOrderByCreatedOnAsc().stream().map(this::entityToDto).toList();
    }

    @Override
    public MovementDto get(long id) throws MovementNotFoundException {
        var movement =
                teamMovementRepository.findById(id).orElseThrow(() -> new MovementNotFoundException(id));
        return entityToDto(movement);
    }

    @Override
    public MovementDto create(MovementType type, double amount, String description) throws MemberNotFoundException {
        var movementEntity =
                teamMovementRepository.save(
                        TeamMovementEntity.builder()
                                .type(type)
                                .amount(amount)
                                .description(description)
                                .build());

        return new MovementDto(
                movementEntity.getId(),
                movementEntity.getType(),
                "",
                movementEntity.getAmount(),
                movementEntity.getDescription(),
                formatter.format(movementEntity.getCreatedOn()));
    }

    @Override
    public MovementDto update(long id, double amount, String description) throws MovementNotFoundException, MemberNotFoundException {
        var movement =
                teamMovementRepository.findById(id).orElseThrow(() -> new MovementNotFoundException(id));

        var movementUpdated =
                teamMovementRepository.save(
                        TeamMovementEntity.builder()
                                .id(movement.getId())
                                .type(movement.getType())
                                .amount(amount)
                                .description(description)
                                .createdOn(movement.getCreatedOn())
                                .build());

        return new MovementDto(
                movementUpdated.getId(),
                movementUpdated.getType(),
                "",
                movementUpdated.getAmount(),
                movementUpdated.getDescription(),
                formatter.format(movementUpdated.getCreatedOn()));
    }

    @Override
    public void delete(long id) {
        teamMovementRepository.deleteById(id);
    }

    @Override
    public TotalBalanceDto getTotalBalance() {
        List<MovementEntity> membersMovements = movementRepository.findAll();

        double membersTotalIncomes = membersMovements.stream()
                .filter(movement -> movement.getType() == MovementType.INCOME)
                .mapToDouble(MovementEntity::getAmount)
                .sum();

        List<TeamMovementEntity> teamMovements = teamMovementRepository.findAll();

        double teamTotalExpenses = teamMovements.stream()
                .filter(movement -> movement.getType() == MovementType.EXPENSE)
                .mapToDouble(TeamMovementEntity::getAmount)
                .sum();
        double teamTotalIncomes = teamMovements.stream()
                .filter(movement -> movement.getType() == MovementType.INCOME)
                .mapToDouble(TeamMovementEntity::getAmount)
                .sum();

    return new TotalBalanceDto(
            teamTotalExpenses,
            membersTotalIncomes + teamTotalIncomes
    );
  }

    private MovementDto entityToDto(TeamMovementEntity entity) {
        return new MovementDto(
                entity.getId(),
                entity.getType(),
                "",
                entity.getAmount(),
                entity.getDescription(),
                formatter.format(entity.getCreatedOn()));
    }
}
