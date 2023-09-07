package com.torresj.footballteammanagementapi.repositories;

import com.torresj.footballteammanagementapi.entities.MovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<MovementEntity, Long> {
    List<MovementEntity> findByMemberId(long memberId);
}
