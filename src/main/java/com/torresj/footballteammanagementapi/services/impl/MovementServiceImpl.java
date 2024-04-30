package com.torresj.footballteammanagementapi.services.impl;

import com.torresj.footballteammanagementapi.dtos.MovementDto;
import com.torresj.footballteammanagementapi.dtos.TotalBalanceDto;
import com.torresj.footballteammanagementapi.entities.MovementEntity;
import com.torresj.footballteammanagementapi.enums.MovementType;
import com.torresj.footballteammanagementapi.exceptions.MemberNotFoundException;
import com.torresj.footballteammanagementapi.exceptions.MovementNotFoundException;
import com.torresj.footballteammanagementapi.repositories.MemberRepository;
import com.torresj.footballteammanagementapi.repositories.MovementRepository;
import com.torresj.footballteammanagementapi.services.MovementService;

import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovementServiceImpl implements MovementService {

    private final MovementRepository movementRepository;
    private final MemberRepository memberRepository;

    @Value("${admin.user}")
    private final String adminUser;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Page<MovementDto> get(Long memberId, String filter, int nElements, int nPage) {
        var pageRequest = PageRequest.of(nPage, nElements, Sort.by(Sort.Direction.DESC, "createdOn"));
        Page<MovementEntity> pageEntity;
        if (memberId == null && filter == null) {
            pageEntity = movementRepository.findAll(pageRequest);
        } else if (memberId != null && filter == null) {
            pageEntity = movementRepository.findByMemberId(memberId, pageRequest);
        } else if (memberId == null) {
            pageEntity = movementRepository.findByDescriptionContainingIgnoreCase(filter, pageRequest);
        } else {
            pageEntity = movementRepository
                    .findByMemberIdAndDescriptionContainingIgnoreCase(memberId, filter, pageRequest);
        }

        return pageEntity.map(this::entityToDto);
    }

    @Override
    public MovementDto get(long id) throws MovementNotFoundException {
        var movement =
                movementRepository.findById(id).orElseThrow(() -> new MovementNotFoundException(id));
        return entityToDto(movement);
    }

    @Override
    public List<MovementDto> getByMember(long memberId) throws MemberNotFoundException {
        memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(""));
        return movementRepository.findByMemberId(memberId, Sort.by(Sort.Direction.DESC, "createdOn")).stream()
                .map(this::entityToDto)
                .toList();
    }

    @Override
    public double getBalance(long memberId) {
        return movementRepository.findByMemberId(memberId, Sort.by(Sort.Direction.DESC, "createdOn")).stream()
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
                                .amount(checkAndReturnAmount(type,amount))
                                .memberId(memberId)
                                .description(description)
                                .build());

        return new MovementDto(
                movementEntity.getId(),
                movementEntity.getType(),
                member.getName() + " " + member.getSurname(),
                movementEntity.getAmount(),
                movementEntity.getDescription(),
                formatter.format(movementEntity.getCreatedOn()));
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
                                .amount(checkAndReturnAmount(movement.getType(),amount))
                                .memberId(movement.getMemberId())
                                .description(description)
                                .createdOn(movement.getCreatedOn())
                                .build());

        return new MovementDto(
                movementUpdated.getId(),
                movementUpdated.getType(),
                member.getName() + " " + member.getSurname(),
                movementUpdated.getAmount(),
                movementUpdated.getDescription(),
                formatter.format(movementUpdated.getCreatedOn()));
    }

    @Override
    public void delete(long id) {
        movementRepository.deleteById(id);
    }

    @Override
    public void addAnnualTeamPay() {
        memberRepository.findAll().stream()
                .filter(member -> !adminUser.equals(member.getName()))
                .forEach(member -> movementRepository.save(
                                MovementEntity.builder()
                                        .type(MovementType.EXPENSE)
                                        .amount(-70)
                                        .memberId(member.getId())
                                        .description("Cuota anual de la peña")
                                        .build()
                        )
                );
    }

    @Override
    public TotalBalanceDto getTotalBalance() {
        List<MovementEntity> movements = movementRepository.findAll();
        double totalExpenses = movements.stream()
                .filter(movement -> movement.getType() == MovementType.EXPENSE)
                .mapToDouble(MovementEntity::getAmount)
                .sum();
        double totalIncomes = movements.stream()
                .filter(movement -> movement.getType() == MovementType.INCOME)
                .mapToDouble(MovementEntity::getAmount)
                .sum();
        return new TotalBalanceDto(totalExpenses, totalIncomes);
    }

    private MovementDto entityToDto(MovementEntity entity) {
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
                formatter.format(entity.getCreatedOn()));
    }

    private double checkAndReturnAmount(MovementType type, double amount){
        if(type.equals(MovementType.EXPENSE)){
            return amount > 0 ? amount * -1 : amount;
        }else{
            return amount < 0 ? amount * -1 : amount;
        }
    }
}
