package com.torresj.footballteammanagementapi.repositories;

import com.torresj.footballteammanagementapi.entities.MovementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<MovementEntity, Long> {
    List<MovementEntity> findByMemberId(long memberId, Sort sort);

    Page<MovementEntity> findByMemberId(long memberId, Pageable page);

    Page<MovementEntity> findByDescriptionContainingIgnoreCase(String filter, Pageable page);

    Page<MovementEntity> findByMemberIdAndDescriptionContainingIgnoreCase(long memberId, String filter, Pageable page);
}
