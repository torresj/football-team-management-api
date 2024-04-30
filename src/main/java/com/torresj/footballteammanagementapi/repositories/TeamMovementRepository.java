package com.torresj.footballteammanagementapi.repositories;

import com.torresj.footballteammanagementapi.entities.MovementEntity;
import java.util.List;

import com.torresj.footballteammanagementapi.entities.TeamMovementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamMovementRepository extends JpaRepository<TeamMovementEntity, Long> {
    List<TeamMovementEntity> findByOrderByCreatedOnAsc();
}
