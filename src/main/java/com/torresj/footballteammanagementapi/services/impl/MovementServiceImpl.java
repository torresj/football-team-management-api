package com.torresj.footballteammanagementapi.services.impl;

import com.torresj.footballteammanagementapi.dtos.MovementDto;
import com.torresj.footballteammanagementapi.entities.MovementEntity;
import com.torresj.footballteammanagementapi.enums.MovementType;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.exceptions.MovementNotFoundException;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.repositories.MovementRepository;
import com.torresj.footballteammanagementapi.services.MovementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovementServiceImpl implements MovementService {

  private final MovementRepository movementRepository;
  private final MemberRepository memberRepository;

  @Override
  public List<MovementDto> get() {
    return movementRepository.findAll().stream()
        .map(
            entity -> {
              var member = memberRepository.findById(entity.getMemberId());
              String memberName =
                  member
                      .map(memberEntity -> memberEntity.getName() + " " + memberEntity.getSurname())
                      .orElse("Not found");
              return new MovementDto(
                  entity.getId(),
                  entity.getType(),
                  memberName,
                  entity.getAmount(),
                  entity.getDescription(),
                  entity.getCreatedOn());
            })
        .toList();
  }

  @Override
  public MovementDto get(long id) throws MovementNotFoundException {
    var movement =
        movementRepository.findById(id).orElseThrow(() -> new MovementNotFoundException(id));
    var member = memberRepository.findById(movement.getMemberId());
    String memberName =
        member
            .map(memberEntity -> memberEntity.getName() + " " + memberEntity.getSurname())
            .orElse("Not found");

    return new MovementDto(
        movement.getId(),
        movement.getType(),
        memberName,
        movement.getAmount(),
        movement.getDescription(),
        movement.getCreatedOn());
  }

  @Override
  public List<MovementDto> getByMember(long memberId) throws MemberNotFoundException {
    memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(""));
    return movementRepository.findByMemberId(memberId).stream()
        .map(
            movementEntity -> {
              var member = memberRepository.findById(movementEntity.getMemberId());
              String memberName =
                  member
                      .map(memberEntity -> memberEntity.getName() + " " + memberEntity.getSurname())
                      .orElse("Not found");
              return new MovementDto(
                  movementEntity.getId(),
                  movementEntity.getType(),
                  memberName,
                  movementEntity.getAmount(),
                  movementEntity.getDescription(),
                  movementEntity.getCreatedOn());
            })
        .toList();
  }

  @Override
  public double getBalance(long memberId) {
    return movementRepository.findByMemberId(memberId).stream()
        .mapToDouble(MovementEntity::getAmount)
        .sum();
  }

  @Override
  public MovementDto create(long memberId, MovementType type, double amount, String description)
      throws MemberNotFoundException {
    var member =
        memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(""));

    var movementEntity =
        movementRepository.save(
            MovementEntity.builder()
                .type(type)
                .amount(amount)
                .memberId(memberId)
                .description(description)
                .build());

    return new MovementDto(
        movementEntity.getId(),
        movementEntity.getType(),
        member.getName() + " " + member.getSurname(),
        movementEntity.getAmount(),
        movementEntity.getDescription(),
        movementEntity.getCreatedOn());
  }

  @Override
  public MovementDto update(long id, double amount, String description)
      throws MovementNotFoundException, MemberNotFoundException {
    var movement =
        movementRepository.findById(id).orElseThrow(() -> new MovementNotFoundException(id));

    var member =
        memberRepository
            .findById(movement.getMemberId())
            .orElseThrow(() -> new MemberNotFoundException(""));

    var movementUpdated =
        movementRepository.save(
            MovementEntity.builder()
                .id(movement.getId())
                .type(movement.getType())
                .amount(amount)
                .memberId(movement.getMemberId())
                .description(description)
                .build());

    return new MovementDto(
        movementUpdated.getId(),
        movementUpdated.getType(),
        member.getName() + " " + member.getSurname(),
        movementUpdated.getAmount(),
        movementUpdated.getDescription(),
        movementUpdated.getCreatedOn());
  }

  @Override
  public void delete(long id) {
    movementRepository.deleteById(id);
  }
}
